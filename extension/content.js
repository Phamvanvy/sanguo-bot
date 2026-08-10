(() => {
  if (document.querySelector("#sanguo-flow-panel")) return;

  const panel = document.createElement("aside");
  panel.id = "sanguo-flow-panel";
  panel.setAttribute("popover", "manual");
  panel.innerHTML = `
    <header class="sg-header">
      <span class="sg-crest">三</span>
      <span class="sg-title"><strong>Sanguo Flows</strong><small>Điều khiển bot nhiệm vụ</small></span>
      <span class="sg-dot sg-offline" title="Controller"></span>
      <button class="sg-collapse" type="button" title="Thu gọn">−</button>
    </header>
    <div class="sg-body">
      <div class="sg-status">Đang kết nối controller…</div>
      <section class="sg-flows"></section>
      <button class="sg-stop" type="button" disabled>Dừng flow đang chạy</button>
      <footer>Kéo thanh tiêu đề để di chuyển · Bấm − để thu gọn</footer>
    </div>`;
  document.documentElement.append(panel);
  try { panel.showPopover(); } catch (_) { panel.style.display = "block"; }

  const header = panel.querySelector(".sg-header");
  const body = panel.querySelector(".sg-body");
  const flowsNode = panel.querySelector(".sg-flows");
  const statusNode = panel.querySelector(".sg-status");
  const dot = panel.querySelector(".sg-dot");
  const collapseButton = panel.querySelector(".sg-collapse");
  const stopButton = panel.querySelector(".sg-stop");
  let flows = [];
  let dragging = null;

  function send(message) {
    return new Promise((resolve, reject) => {
      chrome.runtime.sendMessage(message, (response) => {
        const error = chrome.runtime.lastError;
        if (error) reject(new Error(error.message));
        else if (response?.error) reject(new Error(response.error));
        else resolve(response);
      });
    });
  }

  const api = (path, options = {}) => send({
    type: "api",
    path,
    options,
    fullscreen: Boolean(document.fullscreenElement),
  });

  function setConnected(connected) {
    dot.classList.toggle("sg-online", connected);
    dot.classList.toggle("sg-offline", !connected);
  }

  function renderFlows(running) {
    flowsNode.replaceChildren(...flows.map((flow) => {
      const button = document.createElement("button");
      button.className = "sg-flow";
      button.type = "button";
      button.disabled = running;
      button.innerHTML = `<span></span><strong></strong><small></small>`;
      button.querySelector("span").textContent = flow.icon;
      button.querySelector("strong").textContent = flow.label;
      button.querySelector("small").textContent = flow.description;
      button.addEventListener("click", () => runFlow(flow));
      return button;
    }));
  }

  function showStatus(data) {
    const running = data.state === "running" || data.state === "stopping";
    panel.classList.toggle("sg-running", running);
    statusNode.classList.toggle("sg-active", running);
    statusNode.textContent = data.message || (running ? `Đang chạy: ${data.flow}` : "Sẵn sàng");
    stopButton.disabled = !running;
    renderFlows(running);
  }

  async function runFlow(flow) {
    try {
      let result;
      if ([
        "blessing_loop",
        "code_redeem_loop",
        "discard_loop",
        "use_item_loop",
        "coin_shake_loop",
      ].includes(flow.runner)) {
        const config = await api(`/macro?id=${encodeURIComponent(flow.id)}`);
        result = await send({ type: "run-native", flow: flow.id, macro: config.macro });
      } else {
        result = await api("/run", { method: "POST", body: JSON.stringify({ flow: flow.id }) });
      }
      showStatus(result);
    } catch (error) {
      showStatus({ state: "error", message: error.message });
    }
  }

  async function stopFlow() {
    try {
      const native = await send({ type: "native-status" });
      const result = native.state === "running" || native.state === "stopping"
        ? await send({ type: "stop-native" })
        : await api("/stop", { method: "POST", body: "{}" });
      showStatus(result);
    } catch (error) {
      showStatus({ state: "error", message: error.message });
    }
  }

  async function refresh() {
    try {
      if (!flows.length) flows = (await api("/flows")).flows;
      const native = await send({ type: "native-status" });
      const status = native.state !== "idle" ? native : await api("/status");
      setConnected(true);
      showStatus(status);
    } catch (_) {
      setConnected(false);
      showStatus({ state: "error", message: "Controller chưa chạy. Mở start-extension-server.ps1" });
    }
  }

  function savePanelState() {
    localStorage.setItem("sanguo-flow-panel", JSON.stringify({
      left: panel.style.left,
      top: panel.style.top,
      collapsed: panel.classList.contains("sg-collapsed"),
    }));
  }

  function restorePanelState() {
    try {
      const state = JSON.parse(localStorage.getItem("sanguo-flow-panel") || "{}");
      if (state.left) {
        panel.style.left = state.left;
        panel.style.right = "auto";
      }
      if (state.top) panel.style.top = state.top;
      if (state.collapsed) panel.classList.add("sg-collapsed");
    } catch (_) { /* Ignore stale state. */ }
    collapseButton.textContent = panel.classList.contains("sg-collapsed") ? "+" : "−";
  }

  header.addEventListener("pointerdown", (event) => {
    if (event.target.closest("button")) return;
    const rect = panel.getBoundingClientRect();
    dragging = { dx: event.clientX - rect.left, dy: event.clientY - rect.top };
    header.setPointerCapture(event.pointerId);
  });
  header.addEventListener("pointermove", (event) => {
    if (!dragging) return;
    const maxLeft = Math.max(0, innerWidth - panel.offsetWidth);
    const maxTop = Math.max(0, innerHeight - panel.offsetHeight);
    panel.style.left = `${Math.min(maxLeft, Math.max(0, event.clientX - dragging.dx))}px`;
    panel.style.top = `${Math.min(maxTop, Math.max(0, event.clientY - dragging.dy))}px`;
    panel.style.right = "auto";
  });
  header.addEventListener("pointerup", () => { dragging = null; savePanelState(); });
  header.addEventListener("pointercancel", () => { dragging = null; });
  collapseButton.addEventListener("click", () => {
    panel.classList.toggle("sg-collapsed");
    collapseButton.textContent = panel.classList.contains("sg-collapsed") ? "+" : "−";
    savePanelState();
  });
  stopButton.addEventListener("click", stopFlow);
  chrome.runtime.onMessage.addListener((message) => {
    if (message.type === "toggle-panel") panel.classList.toggle("sg-hidden");
  });
  document.addEventListener("fullscreenchange", () => {
    if (typeof panel.showPopover === "function") {
      try { panel.hidePopover(); panel.showPopover(); } catch (_) { /* Already visible. */ }
    }
  });

  restorePanelState();
  refresh();
  setInterval(refresh, 1200);
})();
