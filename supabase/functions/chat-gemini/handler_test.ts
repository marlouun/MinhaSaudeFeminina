import { createHandler, MODEL } from "./handler.ts";
const assert = (value: unknown, message = "assertion failed") => { if (!value) throw new Error(message); };
const session = "11111111-1111-4111-8111-111111111111";
const token = "header." + btoa(JSON.stringify({ session_id: session })) + ".signature";
const user = "22222222-2222-4222-8222-222222222222";
function setup(options: { authStatus?: number; quota?: string; quotaError?: boolean; upstreamStatus?: number; anonymous?: boolean; rotatedServer?: boolean } = {}) {
  const calls: { url: string; init?: RequestInit }[] = [];
  const env: Record<string, string> = { SUPABASE_URL: "https://example.invalid", SUPABASE_SERVICE_ROLE_KEY: "server-secret", GEMINI_API_KEY: "gemini-secret" };
  const handler = createHandler({ env: name => env[name], fetch: ((url: string, init?: RequestInit) => {
    calls.push({ url, init });
    if (url.endsWith("/user")) return Promise.resolve(Response.json({ id: user, is_anonymous: options.anonymous ?? false, email_confirmed_at: "2026-01-01" }, { status: options.authStatus ?? 200 }));
    if (url.includes("reserve_chat_quota") && options.authStatus === 401 && !options.rotatedServer) return Promise.resolve(Response.json({}, { status: 403 }));
    if (url.includes("reserve_chat_quota")) return Promise.resolve(Response.json({ status: options.quota ?? "ok" }, { status: options.quotaError ? 500 : 200 }));
    return Promise.resolve(Response.json({ candidates: [{ content: { parts: [{ text: "Teste concluído." }] } }] }, { status: options.upstreamStatus ?? 200 }));
  }) as typeof fetch });
  return { handler, calls };
}
function request(body: unknown = { message: "Quanto é dois mais dois?", consent: true }, auth: string | null = token) {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  if (auth !== null) headers.Authorization = "Bearer " + auth;
  return new Request("https://example.invalid/chat", { method: "POST", headers, body: JSON.stringify(body) });
}
Deno.test("public key alone never calls Auth, SQL or Gemini", async () => {
  const { handler, calls } = setup();
  assert((await handler(request(undefined, null))).status === 401);
  assert(calls.length === 0);
});
Deno.test("invalid token never reaches Gemini", async () => {
  const { handler, calls } = setup({ authStatus: 401 });
  assert((await handler(request())).status === 401);
  assert(calls.length === 2);
});
Deno.test("anonymous authenticated user can spend quota without email", async () => {
  const { handler, calls } = setup({ anonymous: true });
  assert((await handler(request())).status === 200);
  assert(calls.length === 3);
  const reservation = JSON.parse(calls[1].init!.body as string);
  assert(reservation.p_user_id === user);
  assert(reservation.p_session_id === session);
});
Deno.test("revoked session is rejected before Gemini", async () => {
  const { handler, calls } = setup({ quota: "forbidden" });
  assert((await handler(request())).status === 403);
  assert(calls.length === 2);
  assert(JSON.parse(calls[1].init!.body as string).p_session_id === session);
});
Deno.test("quota limits and database failures fail closed", async () => {
  for (const [options, status] of [[{ quota: "limited" }, 429], [{ quotaError: true }, 503], [{ quota: "disabled" }, 503]] as const) {
    const { handler, calls } = setup(options);
    assert((await handler(request())).status === status);
    assert(calls.length === 2);
  }
});
Deno.test("malformed payload, missing consent and excessive history reserve nothing", async () => {
  for (const body of [null, { message: "test", consent: false }, { message: "test", consent: true, history: Array(7).fill({ role: "user", text: "x" }) }, { message: "test", consent: true, history: [{ role: "system", text: "x" }] }]) {
    const { handler, calls } = setup();
    assert((await handler(request(body))).status === 400);
    assert(calls.length === 1);
  }
});
Deno.test("actual body limit works without Content-Length", async () => {
  const { handler, calls } = setup();
  assert((await handler(request({ message: "x".repeat(19000), consent: true }))).status === 413);
  assert(calls.length === 1);
});
Deno.test("authorized requests reserve once then use configured model", async () => {
  const { handler, calls } = setup();
  const response = await handler(request());
  assert(response.status === 200);
  assert((await response.json()).answer === "Teste concluído.");
  assert(calls.length === 3 && calls[2].url.includes(MODEL));
  assert(calls[2].init!.headers && !JSON.stringify(calls[1]).includes("Quanto é"));
});
Deno.test("upstream quota failure is actionable without revealing credentials", async () => {
  const { handler } = setup({ upstreamStatus: 429 });
  const response = await handler(request());
  const text = await response.text();
  assert(response.status === 429 && text.includes('"providerStatus":429'));
  assert(!text.includes("secret"));
});
Deno.test("server-only test still requires quota reservation", async () => {
  const { handler, calls } = setup();
  assert((await handler(request(undefined, "server-secret"))).status === 200);
  assert(calls.length === 2 && calls[0].url.includes("reserve_chat_quota"));
});

Deno.test("rotated server credential must pass the service-only RPC", async () => {
  const { handler, calls } = setup({ authStatus: 401, rotatedServer: true });
  assert((await handler(request())).status === 200);
  assert(calls.length === 3);
  assert((calls[1].init!.headers as Record<string, string>).Authorization === "Bearer " + token);
});

Deno.test("conversation context reaches Gemini in order before the follow-up", async () => {
  const { handler, calls } = setup({ anonymous: true });
  const history = [
    { role: "user", text: "Vamos falar do número 14." },
    { role: "model", text: "14 é um número par." },
  ];
  assert((await handler(request({ message: "Qual é a metade desse número?", consent: true, history }))).status === 200);
  const payload = JSON.parse(calls[2].init!.body as string);
  assert(payload.contents.length === 3);
  assert(payload.contents[0].role === "user" && payload.contents[0].parts[0].text === history[0].text);
  assert(payload.contents[1].role === "model" && payload.contents[1].parts[0].text === history[1].text);
  assert(payload.contents[2].parts[0].text === "Qual é a metade desse número?");
  assert(!JSON.stringify(calls[1]).includes("número"));
});
