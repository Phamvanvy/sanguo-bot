(() => {
  if (window.__SANGUO_NETWORK_PROBE__) return;
  window.__SANGUO_NETWORK_PROBE__ = true;

  const STORAGE_KEY = "sanguo-last-network-event";
  const NativeWebSocket = window.WebSocket;
  if (typeof NativeWebSocket !== "function") return;

  function remember(event) {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ ...event, at: Date.now() }));
    } catch (_) { /* Storage may be unavailable during teardown. */ }
  }

  function ProbedWebSocket(url, protocols) {
    const socket = protocols === undefined
      ? new NativeWebSocket(url)
      : new NativeWebSocket(url, protocols);
    socket.addEventListener("error", () => remember({ type: "error", url: String(url) }));
    socket.addEventListener("close", (event) => remember({
      type: "close",
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

  const watchGuard = () => {
    if (document.getElementById("__mch5_guard")) remember({ type: "guard" });
  };
  new MutationObserver(watchGuard).observe(document.documentElement, { childList: true, subtree: true });
})();
