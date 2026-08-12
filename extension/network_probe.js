(() => {
  if (window.__SANGUO_NETWORK_PROBE__) return;
  window.__SANGUO_NETWORK_PROBE__ = true;

  const LAST_EVENT_KEY = "sanguo-last-network-event";
  const EVENT_QUEUE_KEY = "sanguo-network-event-queue";
  const FLOW_CONTEXT_KEY = "sanguo-flow-context";
  const pageSession = crypto.randomUUID?.() || `${Date.now()}-${Math.random()}`;

  function readJson(key, fallback) {
    try { return JSON.parse(localStorage.getItem(key) || "null") ?? fallback; }
    catch (_) { return fallback; }
  }

  function remember(event) {
    try {
      const flow = readJson(FLOW_CONTEXT_KEY, {});
      const entry = {
        id: crypto.randomUUID?.() || `${Date.now()}-${Math.random()}`,
        at: Date.now(),
        pageSession,
        pageUrl: location.href,
        type: event.type || "unknown",
        navigatorOnline: navigator.onLine !== false,
        visibility: document.visibilityState,
        documentState: document.readyState,
        perfMs: Math.round(performance.now()),
        ...flow,
        ...event,
      };
      const queue = readJson(EVENT_QUEUE_KEY, []);
      queue.push(entry);
      localStorage.setItem(EVENT_QUEUE_KEY, JSON.stringify(queue.slice(-100)));
      localStorage.setItem(LAST_EVENT_KEY, JSON.stringify(entry));
    } catch (_) { /* Storage may be unavailable during teardown. */ }
  }

  const NativeWebSocket = window.WebSocket;
  if (typeof NativeWebSocket === "function") {
    function ProbedWebSocket(url, protocols) {
      const socket = protocols === undefined
        ? new NativeWebSocket(url)
        : new NativeWebSocket(url, protocols);
      socket.addEventListener("open", () => remember({ type: "ws_open", url: String(url) }));
      socket.addEventListener("error", () => remember({ type: "ws_error", url: String(url) }));
      socket.addEventListener("close", (event) => remember({
        type: "ws_close",
        url: String(url),
        code: Number(event.code),
        reason: String(event.reason || ""),
        clean: Boolean(event.wasClean),
      }));
      return socket;
    }

    ProbedWebSocket.prototype = NativeWebSocket.prototype;
    for (const property of ["CONNECTING", "OPEN", "CLOSING", "CLOSED"]) {
      Object.defineProperty(ProbedWebSocket, property, { value: NativeWebSocket[property] });
    }
    window.WebSocket = ProbedWebSocket;
  }

  addEventListener("online", () => remember({ type: "browser_online" }));
  addEventListener("offline", () => remember({ type: "browser_offline" }));
  addEventListener("pagehide", (event) => remember({
    type: "page_hide", persisted: Boolean(event.persisted),
  }));
  addEventListener("pageshow", (event) => remember({
    type: "page_show", persisted: Boolean(event.persisted),
  }));
  addEventListener("error", (event) => remember({
    type: "window_error",
    message: String(event.message || ""),
    filename: String(event.filename || ""),
    line: Number(event.lineno || 0),
    column: Number(event.colno || 0),
  }));
  addEventListener("unhandledrejection", (event) => remember({
    type: "unhandled_rejection", message: String(event.reason?.message || event.reason || ""),
  }));

  const watchGuard = () => {
    if (document.getElementById("__mch5_guard")) remember({ type: "guard" });
  };
  new MutationObserver(watchGuard).observe(document.documentElement, { childList: true, subtree: true });
})();
