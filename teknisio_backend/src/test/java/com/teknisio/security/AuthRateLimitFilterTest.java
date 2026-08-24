package com.teknisio.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitFilterTest {

  @Test
  void repeatedLoginFromSameAddressIsRejected() throws Exception {
    AuthRateLimitFilter filter = newFilter(1, 10);

    Result first = execute(filter, "POST", AuthRateLimitFilter.LOGIN_PATH, "192.0.2.10");
    Result second = execute(filter, "POST", AuthRateLimitFilter.LOGIN_PATH, "192.0.2.10");

    assertThat(first.filterChainReached()).isTrue();
    assertThat(second.filterChainReached()).isFalse();
    assertThat(second.response().getStatus()).isEqualTo(429);
    assertThat(second.response().getHeader("Retry-After")).isNotBlank();
    assertThat(second.response().getContentAsString()).contains(AuthRateLimitFilter.RATE_LIMIT_MESSAGE);
  }

  @Test
  void loginBucketsAreIndependentPerAddress() throws Exception {
    AuthRateLimitFilter filter = newFilter(1, 10);

    Result firstClient = execute(filter, "POST", AuthRateLimitFilter.LOGIN_PATH, "192.0.2.10");
    Result secondClient = execute(filter, "POST", AuthRateLimitFilter.LOGIN_PATH, "192.0.2.11");

    assertThat(firstClient.filterChainReached()).isTrue();
    assertThat(secondClient.filterChainReached()).isTrue();
  }

  @Test
  void customerAndTechnicianRegistrationShareOneAddressBucket() throws Exception {
    AuthRateLimitFilter filter = newFilter(10, 1);

    Result first = execute(
      filter,
      "POST",
      "/api/auth/register/customer",
      "198.51.100.20"
    );
    Result second = execute(
      filter,
      "POST",
      "/api/auth/register/technician",
      "198.51.100.20"
    );

    assertThat(first.filterChainReached()).isTrue();
    assertThat(second.filterChainReached()).isFalse();
    assertThat(second.response().getStatus()).isEqualTo(429);
  }

  @Test
  void nonAuthenticationRequestIsNotLimited() throws Exception {
    AuthRateLimitFilter filter = newFilter(1, 1);

    Result result = execute(filter, "GET", "/api/device-categories", "192.0.2.10");

    assertThat(result.filterChainReached()).isTrue();
  }

  private static AuthRateLimitFilter newFilter(long loginCapacity, long registrationCapacity) {
    return new AuthRateLimitFilter(
      new ObjectMapper(),
      loginCapacity,
      60,
      registrationCapacity,
      3600,
      100
    );
  }

  private static Result execute(
    AuthRateLimitFilter filter,
    String method,
    String path,
    String remoteAddress
  ) throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest(method, path);
    request.setServletPath(path);
    request.setRemoteAddr(remoteAddress);
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicBoolean filterChainReached = new AtomicBoolean(false);

    filter.doFilter(request, response, (ignoredRequest, ignoredResponse) ->
      filterChainReached.set(true)
    );

    return new Result(filterChainReached.get(), response);
  }

  private record Result(
    boolean filterChainReached,
    MockHttpServletResponse response
  ) {
  }
}
