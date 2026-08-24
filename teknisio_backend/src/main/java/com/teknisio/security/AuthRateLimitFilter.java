package com.teknisio.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.teknisio.common.response.ApiResponse;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Per-instance, per-IP limiter for public authentication endpoints.
 *
 * <p>The cache is bounded to prevent untrusted client addresses from causing
 * unbounded memory growth. A distributed deployment should enforce an
 * additional shared limit at the trusted reverse proxy or API gateway.</p>
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

  static final String LOGIN_PATH = "/api/auth/login";
  static final Set<String> REGISTRATION_PATHS = Set.of(
    "/api/auth/register/customer",
    "/api/auth/register/technician"
  );
  static final String RATE_LIMIT_MESSAGE = "Too many authentication requests. Try again later.";

  private final ObjectMapper objectMapper;
  private final Cache<String, Bucket> loginBuckets;
  private final Cache<String, Bucket> registrationBuckets;
  private final long loginCapacity;
  private final Duration loginWindow;
  private final long registrationCapacity;
  private final Duration registrationWindow;

  public AuthRateLimitFilter(
    ObjectMapper objectMapper,
    @Value("${app.security.auth-rate-limit.login-capacity:5}") long loginCapacity,
    @Value("${app.security.auth-rate-limit.login-window-seconds:60}") long loginWindowSeconds,
    @Value("${app.security.auth-rate-limit.registration-capacity:10}") long registrationCapacity,
    @Value("${app.security.auth-rate-limit.registration-window-seconds:3600}") long registrationWindowSeconds,
    @Value("${app.security.auth-rate-limit.max-clients:10000}") long maxClients
  ) {
    validatePositive("login-capacity", loginCapacity);
    validatePositive("login-window-seconds", loginWindowSeconds);
    validatePositive("registration-capacity", registrationCapacity);
    validatePositive("registration-window-seconds", registrationWindowSeconds);
    validatePositive("max-clients", maxClients);

    this.objectMapper = objectMapper;
    this.loginCapacity = loginCapacity;
    this.loginWindow = Duration.ofSeconds(loginWindowSeconds);
    this.registrationCapacity = registrationCapacity;
    this.registrationWindow = Duration.ofSeconds(registrationWindowSeconds);
    this.loginBuckets = newBucketCache(maxClients, this.loginWindow);
    this.registrationBuckets = newBucketCache(maxClients, this.registrationWindow);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    if (!HttpMethod.POST.matches(request.getMethod())) {
      return true;
    }

    String path = request.getServletPath();
    return !LOGIN_PATH.equals(path) && !REGISTRATION_PATHS.contains(path);
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    boolean loginRequest = LOGIN_PATH.equals(request.getServletPath());
    Cache<String, Bucket> buckets = loginRequest ? loginBuckets : registrationBuckets;
    long capacity = loginRequest ? loginCapacity : registrationCapacity;
    Duration window = loginRequest ? loginWindow : registrationWindow;
    String clientKey = normalizeClientAddress(request.getRemoteAddr());

    Bucket bucket = buckets.get(clientKey, ignored -> newBucket(capacity, window));
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    if (probe.isConsumed()) {
      filterChain.doFilter(request, response);
      return;
    }

    long retryAfterSeconds = Math.max(
      1,
      (long) Math.ceil((double) probe.getNanosToWaitForRefill() / TimeUnit.SECONDS.toNanos(1))
    );

    response.setStatus(429);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
    response.setHeader("Cache-Control", "no-store");
    objectMapper.writeValue(response.getOutputStream(), ApiResponse.error(RATE_LIMIT_MESSAGE));
  }

  private static Cache<String, Bucket> newBucketCache(long maxClients, Duration expiry) {
    return Caffeine.newBuilder()
      .maximumSize(maxClients)
      .expireAfterAccess(expiry)
      .build();
  }

  private static Bucket newBucket(long capacity, Duration window) {
    return Bucket.builder()
      .addLimit(limit -> limit.capacity(capacity).refillIntervally(capacity, window))
      .build();
  }

  private static String normalizeClientAddress(String remoteAddress) {
    return remoteAddress == null || remoteAddress.isBlank() ? "unknown" : remoteAddress;
  }

  private static void validatePositive(String property, long value) {
    if (value <= 0) {
      throw new IllegalArgumentException(property + " must be greater than zero");
    }
  }
}
