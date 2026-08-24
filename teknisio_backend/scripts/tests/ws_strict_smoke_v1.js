#!/usr/bin/env node

const BASE_URL = process.env.BASE_URL || "http://localhost:8080";
const WS_URL = process.env.WS_URL || "ws://localhost:8080/ws/websocket";
const RUN_ID = `${Date.now()}${Math.floor(Math.random() * 1000)}`;
const PASSWORD = process.env.TEKNISIO_TEST_PASSWORD;

if (!PASSWORD || PASSWORD.length < 12) {
  console.error("TEKNISIO_TEST_PASSWORD wajib diisi minimal 12 karakter untuk fixture sementara.");
  process.exit(2);
}

const WebSocket = require("ws");

let passed = 0;
let failed = 0;

function green(s) {
  console.log(`\x1b[32m${s}\x1b[0m`);
}

function red(s) {
  console.log(`\x1b[31m${s}\x1b[0m`);
}

function pass(name) {
  passed += 1;
  green(`PASS ${name}`);
}

function fail(name, detail = "") {
  failed += 1;
  red(`FAIL ${name}`);
  if (detail) {
    console.log(detail);
  }
  process.exit(1);
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function http(method, path, body = null, token = null, expectedStatus = null) {
  const headers = {
    Accept: "application/json",
  };

  if (body !== null) {
    headers["Content-Type"] = "application/json";
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body === null ? undefined : JSON.stringify(body),
  });

  const text = await res.text();

  let json = null;
  try {
    json = text ? JSON.parse(text) : null;
  } catch {
    fail(`${method} ${path} response is not JSON`, text);
  }

  if (expectedStatus !== null && res.status !== expectedStatus) {
    fail(
      `${method} ${path} expected ${expectedStatus} got ${res.status}`,
      JSON.stringify(json, null, 2)
    );
  }

  return {
    status: res.status,
    json,
  };
}

function assert(condition, name, detail = "") {
  if (!condition) {
    fail(name, detail);
  }

  pass(name);
}

function encodeFrame(command, headers = {}, body = "") {
  const headerLines = Object.entries(headers).map(([k, v]) => `${k}:${v}`);
  return `${command}\n${headerLines.join("\n")}\n\n${body}\0`;
}

function parseFrames(data) {
  const text = data.toString("utf8");

  return text
    .split("\0")
    .filter(Boolean)
    .map((raw) => {
      const separator = raw.indexOf("\n\n");
      const head = separator >= 0 ? raw.slice(0, separator) : raw;
      const body = separator >= 0 ? raw.slice(separator + 2) : "";

      const lines = head.split(/\r?\n/);
      const command = lines.shift();

      const headers = {};
      for (const line of lines) {
        const idx = line.indexOf(":");
        if (idx > -1) {
          headers[line.slice(0, idx)] = line.slice(idx + 1);
        }
      }

      return {
        command,
        headers,
        body,
        raw,
      };
    });
}

class StompClient {
  constructor(name, token) {
    this.name = name;
    this.token = token;
    this.ws = null;
    this.frames = [];
    this.waiters = [];
    this.subId = 0;
  }

  connect(timeoutMs = 3000) {
    return new Promise((resolve, reject) => {
      const ws = new WebSocket(WS_URL);
      this.ws = ws;

      const timeout = setTimeout(() => {
        reject(new Error(`${this.name} connect timeout`));
      }, timeoutMs);

      ws.on("open", () => {
        ws.send(
          encodeFrame("CONNECT", {
            "accept-version": "1.2",
            "heart-beat": "0,0",
            Authorization: `Bearer ${this.token}`,
          })
        );
      });

      ws.on("message", (data) => {
        const frames = parseFrames(data);

        for (const frame of frames) {
          this.frames.push(frame);
          this._notify(frame);

          if (frame.command === "CONNECTED") {
            clearTimeout(timeout);
            resolve();
          }

          if (frame.command === "ERROR") {
            clearTimeout(timeout);
            reject(new Error(`${this.name} STOMP ERROR ${frame.body || frame.raw}`));
          }
        }
      });

      ws.on("error", (err) => {
        clearTimeout(timeout);
        reject(err);
      });

      ws.on("close", () => {
        this._notify({
          command: "CLOSED",
          headers: {},
          body: "",
          raw: "",
        });
      });
    });
  }

  expectConnectFails(timeoutMs = 1500) {
    return new Promise((resolve, reject) => {
      const ws = new WebSocket(WS_URL);
      let connected = false;

      const timeout = setTimeout(() => {
        if (!connected) {
          try {
            ws.close();
          } catch {}
          resolve();
        }
      }, timeoutMs);

      ws.on("open", () => {
        const headers = {
          "accept-version": "1.2",
          "heart-beat": "0,0",
        };

        if (this.token !== null) {
          headers.Authorization = `Bearer ${this.token}`;
        }

        ws.send(encodeFrame("CONNECT", headers));
      });

      ws.on("message", (data) => {
        for (const frame of parseFrames(data)) {
          if (frame.command === "CONNECTED") {
            connected = true;
            clearTimeout(timeout);

            try {
              ws.close();
            } catch {}

            reject(new Error(`${this.name} unexpectedly connected`));
          }
        }
      });

      ws.on("close", () => {
        if (!connected) {
          clearTimeout(timeout);
          resolve();
        }
      });

      ws.on("error", () => {
        if (!connected) {
          clearTimeout(timeout);
          resolve();
        }
      });
    });
  }

  subscribe(destination) {
    this.subId += 1;

    this.sendFrame("SUBSCRIBE", {
      id: `${this.name}-sub-${this.subId}`,
      destination,
    });
  }

  sendJson(destination, payload) {
    const body = JSON.stringify(payload);

    this.sendFrame(
      "SEND",
      {
        destination,
        "content-type": "application/json",
        "content-length": Buffer.byteLength(body),
      },
      body
    );
  }

  sendFrame(command, headers = {}, body = "") {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      throw new Error(`${this.name} websocket not open`);
    }

    this.ws.send(encodeFrame(command, headers, body));
  }

  waitForMessage(predicate, timeoutMs = 3000) {
    const existing = this.frames.find((f) => f.command === "MESSAGE" && predicate(f));
    if (existing) {
      return Promise.resolve(existing);
    }

    return new Promise((resolve, reject) => {
      const timeout = setTimeout(() => {
        this.waiters = this.waiters.filter((w) => w.resolve !== resolve);
        reject(new Error(`${this.name} waitForMessage timeout`));
      }, timeoutMs);

      this.waiters.push({
        predicate,
        resolve: (frame) => {
          clearTimeout(timeout);
          resolve(frame);
        },
        reject,
      });
    });
  }

  async expectNoMessage(predicate, timeoutMs = 1200) {
    const existing = this.frames.find((f) => f.command === "MESSAGE" && predicate(f));
    if (existing) {
      fail(`${this.name} received forbidden message`, existing.body);
    }

    await new Promise((resolve) => {
      const timeout = setTimeout(resolve, timeoutMs);

      this.waiters.push({
        predicate: (frame) => frame.command === "MESSAGE" && predicate(frame),
        resolve: (frame) => {
          clearTimeout(timeout);
          fail(`${this.name} received forbidden message`, frame.body);
        },
        reject: () => {},
      });
    });

    pass(`${this.name} received no forbidden message`);
  }

  _notify(frame) {
    const waiter = this.waiters.find((w) => {
      try {
        return w.predicate(frame);
      } catch {
        return false;
      }
    });

    if (waiter) {
      this.waiters = this.waiters.filter((w) => w !== waiter);
      waiter.resolve(frame);
    }
  }

  close() {
    try {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.sendFrame("DISCONNECT");
        this.ws.close();
      }
    } catch {}
  }
}

async function setupData() {
  const health = await http("GET", "/actuator/health", null, null, 200);
  assert(health.json.status === "UP", "health UP");

  const categories = await http("GET", "/api/device-categories", null, null, 200);
  const ac = categories.json.data.find((c) => c.name === "Air Conditioner");

  assert(Boolean(ac), "Air Conditioner category exists");

  const suffix = RUN_ID.slice(-10);

  const customerBody = {
    name: `WS Customer ${RUN_ID}`,
    email: `ws.customer.${RUN_ID}@mail.com`,
    phoneNumber: `0813${suffix}`,
    password: PASSWORD,
    address: "Jl. WS Customer",
  };

  const otherCustomerBody = {
    name: `WS Other Customer ${RUN_ID}`,
    email: `ws.other.customer.${RUN_ID}@mail.com`,
    phoneNumber: `0815${suffix}`,
    password: PASSWORD,
    address: "Jl. WS Other Customer",
  };

  const techBody = {
    name: `WS Technician ${RUN_ID}`,
    email: `ws.technician.${RUN_ID}@mail.com`,
    phoneNumber: `0814${suffix}`,
    password: PASSWORD,
    address: "Jl. WS Technician",
    description: "Technician for strict websocket smoke test",
  };

  const otherTechBody = {
    name: `WS Other Technician ${RUN_ID}`,
    email: `ws.other.technician.${RUN_ID}@mail.com`,
    phoneNumber: `0816${suffix}`,
    password: PASSWORD,
    address: "Jl. WS Other Technician",
    description: "Other technician for strict websocket smoke test",
  };

  const customerReg = await http("POST", "/api/auth/register/customer", customerBody, null, 201);
  const otherCustomerReg = await http("POST", "/api/auth/register/customer", otherCustomerBody, null, 201);
  const techReg = await http("POST", "/api/auth/register/technician", techBody, null, 201);
  const otherTechReg = await http("POST", "/api/auth/register/technician", otherTechBody, null, 201);

  const customerToken = customerReg.json.data.accessToken;
  const otherCustomerToken = otherCustomerReg.json.data.accessToken;
  const techToken = techReg.json.data.accessToken;
  const otherTechToken = otherTechReg.json.data.accessToken;
  const technicianProfileId = techReg.json.data.user.technicianProfileId;

  assert(Boolean(customerToken), "customer token exists");
  assert(Boolean(techToken), "technician token exists");
  assert(Boolean(technicianProfileId), "technicianProfileId exists");

  await http(
    "POST",
    "/api/technicians/device-categories",
    {
      deviceCategoryId: ac.deviceCategoryId,
    },
    techToken,
    201
  );

  const request = await http(
    "POST",
    "/api/customers/service-requests",
    {
      technicianProfileId,
      deviceCategoryIds: [ac.deviceCategoryId],
      issueDescription: "Strict websocket smoke test request",
      address: "Jl. Strict WebSocket Test",
      addressDetail: "Rumah pagar ungu",
    },
    customerToken,
    201
  );

  const serviceRequestId = request.json.data.serviceRequestId;
  assert(Boolean(serviceRequestId), "serviceRequestId exists");

  await http("PATCH", `/api/technicians/service-requests/${serviceRequestId}/accept`, null, techToken, 200);
  await http("PATCH", `/api/technicians/service-requests/${serviceRequestId}/start`, null, techToken, 200);

  return {
    serviceRequestId,
    customerToken,
    otherCustomerToken,
    techToken,
    otherTechToken,
  };
}

async function main() {
  if (typeof fetch !== "function") {
    fail("Node.js fetch is not available. Use Node >= 18.");
  }

  console.log("============================================================");
  console.log("TEKNISIO STRICT WEBSOCKET SMOKE TEST V1");
  console.log(`BASE_URL=${BASE_URL}`);
  console.log(`WS_URL=${WS_URL}`);
  console.log("============================================================");

  const data = await setupData();
  const serviceRequestId = data.serviceRequestId;

  const noTokenClient = new StompClient("no-token", null);
  await noTokenClient.expectConnectFails();
  pass("CONNECT without token rejected");

  const invalidClient = new StompClient("invalid-token", "invalid.token.value");
  await invalidClient.expectConnectFails();
  pass("CONNECT invalid token rejected");

  const customer = new StompClient("customer", data.customerToken);
  const technician = new StompClient("technician", data.techToken);
  const otherCustomer = new StompClient("otherCustomer", data.otherCustomerToken);
  const otherTechnician = new StompClient("otherTechnician", data.otherTechToken);

  await customer.connect();
  pass("CONNECT customer valid");

  await technician.connect();
  pass("CONNECT technician valid");

  await otherCustomer.connect();
  pass("CONNECT other customer valid");

  await otherTechnician.connect();
  pass("CONNECT other technician valid");

  const chatTopic = `/topic/chat/${serviceRequestId}`;
  const chatSend = `/app/chat/send/${serviceRequestId}`;
  const locationTopic = `/topic/location/${serviceRequestId}`;
  const locationSend = `/app/location/update/${serviceRequestId}`;

  customer.subscribe(chatTopic);
  technician.subscribe(chatTopic);
  otherCustomer.subscribe(chatTopic);
  otherTechnician.subscribe(chatTopic);

  customer.subscribe(locationTopic);
  technician.subscribe(locationTopic);
  otherCustomer.subscribe(locationTopic);
  otherTechnician.subscribe(locationTopic);

  await sleep(500);

  const customerChatText = `customer chat ${RUN_ID}`;

  customer.sendJson(chatSend, {
    serviceRequestId,
    message: customerChatText,
  });

  const techReceivedCustomerChat = await technician.waitForMessage(
    (f) => f.body.includes(customerChatText),
    4000
  );

  assert(
    techReceivedCustomerChat.body.includes(customerChatText),
    "technician received customer chat broadcast"
  );

  const customerReceivedOwnChat = await customer.waitForMessage(
    (f) => f.body.includes(customerChatText),
    4000
  );

  assert(
    customerReceivedOwnChat.body.includes(customerChatText),
    "customer received own chat broadcast"
  );

  await otherCustomer.expectNoMessage((f) => f.body.includes(customerChatText), 1200);
  await otherTechnician.expectNoMessage((f) => f.body.includes(customerChatText), 1200);

  const techChatText = `technician chat ${RUN_ID}`;

  technician.sendJson(chatSend, {
    serviceRequestId,
    message: techChatText,
  });

  const customerReceivedTechChat = await customer.waitForMessage(
    (f) => f.body.includes(techChatText),
    4000
  );

  assert(
    customerReceivedTechChat.body.includes(techChatText),
    "customer received technician chat broadcast"
  );

  await otherCustomer.expectNoMessage((f) => f.body.includes(techChatText), 1200);
  await otherTechnician.expectNoMessage((f) => f.body.includes(techChatText), 1200);

  const blockedChatText = `blocked other customer chat ${RUN_ID}`;

  otherCustomer.sendJson(chatSend, {
    serviceRequestId,
    message: blockedChatText,
  });

  await customer.expectNoMessage((f) => f.body.includes(blockedChatText), 1200);
  await technician.expectNoMessage((f) => f.body.includes(blockedChatText), 1200);
  pass("other customer SEND chat blocked");

  const latitude = -6.2 - Math.floor(Math.random() * 1000) / 1000000;
  const longitude = 106.816666 + Math.floor(Math.random() * 1000) / 1000000;
  const timestamp = Date.now();

  technician.sendJson(locationSend, {
    serviceRequestId,
    latitude,
    longitude,
    timestamp,
  });

  const customerReceivedLocation = await customer.waitForMessage((f) => {
    if (!f.body.includes(serviceRequestId)) {
      return false;
    }

    try {
      const body = JSON.parse(f.body);
      return body.latitude === latitude && body.longitude === longitude;
    } catch {
      return false;
    }
  }, 4000);

  assert(
    customerReceivedLocation.body.includes(String(latitude)),
    "customer received live GPS latitude"
  );

  assert(
    customerReceivedLocation.body.includes(String(longitude)),
    "customer received live GPS longitude"
  );

  await otherCustomer.expectNoMessage(
    (f) => f.body.includes(String(latitude)) || f.body.includes(String(longitude)),
    1200
  );

  await otherTechnician.expectNoMessage(
    (f) => f.body.includes(String(latitude)) || f.body.includes(String(longitude)),
    1200
  );

  const blockedLatitudeCustomer = -7.111111;

  customer.sendJson(locationSend, {
    serviceRequestId,
    latitude: blockedLatitudeCustomer,
    longitude: 110.111111,
    timestamp: Date.now(),
  });

  await customer.expectNoMessage((f) => f.body.includes(String(blockedLatitudeCustomer)), 1200);
  await technician.expectNoMessage((f) => f.body.includes(String(blockedLatitudeCustomer)), 1200);
  pass("customer SEND location blocked");

  const blockedLatitudeOtherTech = -8.222222;

  otherTechnician.sendJson(locationSend, {
    serviceRequestId,
    latitude: blockedLatitudeOtherTech,
    longitude: 111.222222,
    timestamp: Date.now(),
  });

  await customer.expectNoMessage((f) => f.body.includes(String(blockedLatitudeOtherTech)), 1200);
  await technician.expectNoMessage((f) => f.body.includes(String(blockedLatitudeOtherTech)), 1200);
  pass("other technician SEND location blocked");

  const restLocation = await http(
    "GET",
    `/api/location/${serviceRequestId}`,
    null,
    data.customerToken,
    200
  );

  assert(restLocation.json.success === true, "REST location after WS update success true");
  assert(restLocation.json.data.latitude === latitude, "REST location latitude matches WS update");
  assert(restLocation.json.data.longitude === longitude, "REST location longitude matches WS update");

  customer.close();
  technician.close();
  otherCustomer.close();
  otherTechnician.close();

  console.log("");
  console.log("============================================================");
  green("ALL STRICT WEBSOCKET SMOKE TESTS V1 PASSED");
  console.log(`Passed: ${passed}`);
  console.log(`Failed: ${failed}`);
  console.log(`serviceRequestId: ${serviceRequestId}`);
  console.log(`latitude: ${latitude}`);
  console.log(`longitude: ${longitude}`);
  console.log("============================================================");
}

main().catch((err) => {
  red("FAIL unhandled error");
  console.error(err);
  process.exit(1);
});
