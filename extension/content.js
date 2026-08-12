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
  const DOM_RUNNERS = new Set([
    "blessing_loop",
    "code_redeem_loop",
    "discard_loop",
    "use_item_loop",
    "coin_shake_loop",
    "auto_attack_loop",
  ]);
  // Running faster than the game's normal UI cadence can saturate its
  // renderer. The site guard measures debugger latency on that same thread
  // and can otherwise mistake a busy frame for an attached debugger, close
  // the game WebSocket, and reload back to the server picker.
  const DOM_SPEED_FACTOR = 0.7;
  const BLESSING_SPEED_FACTOR = 1.0;
  const NETWORK_EVENT_KEY = "sanguo-last-network-event";
  const NETWORK_QUEUE_KEY = "sanguo-network-event-queue";
  const FLOW_CONTEXT_KEY = "sanguo-flow-context";
  let flows = [];
  let dragging = null;
  let domToken = null;
  let domFlow = { state: "idle", message: "Sẵn sàng" };

  function rememberGuardReload() {
    localStorage.setItem(NETWORK_EVENT_KEY, JSON.stringify({ type: "guard", at: Date.now() }));
  }

  function recentNetworkEvent() {
    try {
      const queue = JSON.parse(localStorage.getItem(NETWORK_QUEUE_KEY) || "[]");
      const event = [...queue].reverse().find((item) => (
        ["guard", "ws_close", "ws_error", "browser_offline"].includes(item.type)
        && Date.now() - Number(item.at) < 5 * 60 * 1000
      )) || JSON.parse(localStorage.getItem(NETWORK_EVENT_KEY) || "null");
      return event && Date.now() - Number(event.at) < 5 * 60 * 1000 ? event : null;
    } catch (_) {
      return null;
    }
  }

  function networkEventMessage(event) {
    if (!event) return "";
    if (event.type === "guard" || event.code === 4001 || event.reason === "guard") {
      return "Rớt do guard: WebSocket 4001/guard";
    }
    if (event.type === "ws_close") {
      const reason = event.reason ? ` (${event.reason})` : "";
      return `WebSocket bị đóng: code ${event.code}${reason}`;
    }
    if (event.type === "ws_error") return "WebSocket báo lá»—i trước khi rớt";
    if (event.type === "browser_offline") return "Trình duyệt ghi nhận máy bị offline";
    return "";
  }

  function appendDiagnostic(type, extra = {}) {
    try {
      const context = JSON.parse(localStorage.getItem(FLOW_CONTEXT_KEY) || "{}");
      const queue = JSON.parse(localStorage.getItem(NETWORK_QUEUE_KEY) || "[]");
      queue.push({
        id: crypto.randomUUID?.() || `${Date.now()}-${Math.random()}`,
        at: Date.now(), type, pageUrl: location.href,
        navigatorOnline: navigator.onLine !== false, visibility: document.visibilityState,
        ...context, ...extra,
      });
      localStorage.setItem(NETWORK_QUEUE_KEY, JSON.stringify(queue.slice(-100)));
    } catch (_) { /* Keep the flow running if diagnostics storage fails. */ }
  }

  function updateFlowContext(flow, cycle, step) {
    let previous = {};
    try { previous = JSON.parse(localStorage.getItem(FLOW_CONTEXT_KEY) || "{}"); } catch (_) { /* stale */ }
    const context = {
      flow, cycle, step,
      startedAt: previous.flow === flow ? previous.startedAt : Date.now(),
      updatedAt: Date.now(),
    };
    localStorage.setItem(FLOW_CONTEXT_KEY, JSON.stringify(context));
    return context;
  }

  async function reportQueuedNetworkEvents() {
    let queue;
    try { queue = JSON.parse(localStorage.getItem(NETWORK_QUEUE_KEY) || "[]"); }
    catch (_) { return; }
    for (const event of queue) {
      await api("/network-event", {
        method: "POST",
        body: JSON.stringify({ ...event, extensionVersion: chrome.runtime.getManifest().version }),
      });
      let current;
      try { current = JSON.parse(localStorage.getItem(NETWORK_QUEUE_KEY) || "[]"); }
      catch (_) { current = []; }
      localStorage.setItem(NETWORK_QUEUE_KEY, JSON.stringify(current.filter((item) => item.id !== event.id)));
    }
  }

  function watchGameGuard() {
    const detect = () => {
      if (document.getElementById("__mch5_guard")) rememberGuardReload();
    };
    detect();
    new MutationObserver(detect).observe(document.documentElement, { childList: true, subtree: true });
  }

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
      if (flow.id === "use_inventory_item") {
        const card = document.createElement("div");
        card.className = "sg-flow sg-item-flow";
        card.innerHTML = `<span></span><strong></strong><small></small><div class="sg-item-slots"></div>`;
        card.querySelector("span").textContent = flow.icon;
        card.querySelector("strong").textContent = flow.label;
        card.querySelector("small").textContent = flow.description;
        const slots = card.querySelector(".sg-item-slots");
        for (const [slot, label] of [["left", "Ô trái"], ["right", "Ô phải"]]) {
          const button = document.createElement("button");
          button.type = "button";
          button.disabled = running;
          button.textContent = label;
          button.addEventListener("click", () => runFlow(flow, { item_slot: slot }));
          slots.append(button);
        }
        return card;
      }
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

  const domDelay = (seconds, speedFactor = DOM_SPEED_FACTOR) => new Promise((resolve) => {
    setTimeout(resolve, Math.max(120, Number(seconds) * 1000 * speedFactor));
  });

  function ensureDomActive(token) {
    if (token.cancelled) throw new Error("FLOW_STOPPED");
  }

  function updateDomFlow(flow, message, step = "status") {
    domFlow = { state: "running", flow, message };
    const cycle = Number(message.match(/\d+/)?.[0] || 0);
    updateFlowContext(flow, cycle, step);
    showStatus(domFlow);
  }

  function eventTargetAt(point) {
    const x = Number(point[0]) * innerWidth;
    const y = Number(point[1]) * innerHeight;
    const target = document.elementFromPoint(x, y);
    if (!target) throw new Error(`Không tìm thấy phần tử game tại (${x.toFixed(0)}, ${y.toFixed(0)})`);
    return { target, x, y };
  }

  function dispatchMouse(target, type, x, y, buttons) {
    const EventClass = type.startsWith("pointer") && typeof PointerEvent === "function"
      ? PointerEvent
      : MouseEvent;
    target.dispatchEvent(new EventClass(type, {
      bubbles: true,
      cancelable: true,
      composed: true,
      view: window,
      clientX: x,
      clientY: y,
      screenX: x,
      screenY: y,
      button: 0,
      buttons,
      detail: type === "click" ? 1 : 0,
      pointerId: 1,
      pointerType: "mouse",
      isPrimary: true,
    }));
  }

  async function domClick(token, point) {
    ensureDomActive(token);
    const { target, x, y } = eventTargetAt(point);
    if (typeof target.focus === "function") target.focus({ preventScroll: true });
    // The TeaVM canvas registers only mousedown/mouseup for a click. Emitting
    // pointer/click duplicates calls into its coroutine bridge and eventually
    // throws "Suspension point reached from non-threading context".
    dispatchMouse(target, "mousedown", x, y, 1);
    await new Promise((resolve) => setTimeout(resolve, 40));
    dispatchMouse(target, "mouseup", x, y, 0);
  }

  function dispatchKey(type, key, code, keyCode, modifiers = 0) {
    const target = document.activeElement || window;
    const event = new KeyboardEvent(type, {
      key,
      code,
      bubbles: true,
      cancelable: true,
      composed: true,
      ctrlKey: Boolean(modifiers & 2),
      altKey: Boolean(modifiers & 1),
      shiftKey: Boolean(modifiers & 8),
      metaKey: Boolean(modifiers & 4),
    });
    for (const property of ["keyCode", "which", "charCode"]) {
      try { Object.defineProperty(event, property, { value: keyCode }); } catch (_) { /* Read-only. */ }
    }
    target.dispatchEvent(event);
  }

  async function domPress(token, key, code, keyCode, modifiers = 0) {
    ensureDomActive(token);
    dispatchKey("keydown", key, code, keyCode, modifiers);
    if (key.length === 1) dispatchKey("keypress", key, code, keyCode, modifiers);
    dispatchKey("keyup", key, code, keyCode, modifiers);
  }

  async function domClearAndType(token, text) {
    ensureDomActive(token);
    const target = document.activeElement;
    if (target instanceof HTMLInputElement || target instanceof HTMLTextAreaElement) {
      const descriptor = Object.getOwnPropertyDescriptor(Object.getPrototypeOf(target), "value");
      descriptor?.set?.call(target, "");
      target.dispatchEvent(new InputEvent("input", { bubbles: true, inputType: "deleteContentBackward" }));
      descriptor?.set?.call(target, text);
      target.dispatchEvent(new InputEvent("input", {
        bubbles: true,
        inputType: "insertText",
        data: text,
      }));
      target.dispatchEvent(new Event("change", { bubbles: true }));
      return;
    }
    if (target?.isContentEditable) {
      document.execCommand("selectAll", false);
      document.execCommand("insertText", false, text);
      return;
    }
    await domPress(token, "a", "KeyA", 65, 2);
    await domPress(token, "Backspace", "Backspace", 8);
    for (const character of text) {
      const upper = character.toUpperCase();
      await domPress(token, character, /^[A-Z]$/i.test(character) ? `Key${upper}` : "", character.charCodeAt(0));
    }
  }

  function expandCodes(macro) {
    const codes = (macro.codes || []).map(String).filter(Boolean);
    for (const range of macro.code_ranges || []) {
      for (let number = Number(range.start || 1); number <= Number(range.end || 0); number += 1) {
        codes.push(`${range.prefix || ""}${number}`);
      }
    }
    return codes;
  }

  async function runBlessing(token, macro) {
    updateFlowContext("blessing", 0, "open_panel");
    await domClick(token, macro.open_point || [0.73, 0.07]);
    await domDelay(macro.open_delay_seconds || 2, BLESSING_SPEED_FACTOR);
    const maxCycles = Number(macro.max_cycles || 0);
    const restEvery = Number(macro.rest_every_cycles || 10);
    for (let cycle = 0; maxCycles <= 0 || cycle < maxCycles; cycle += 1) {
      const cycleNumber = cycle + 1;
      for (let click = 0; click < (cycle === 0 ? 1 : 2); click += 1) {
        updateFlowContext("blessing", cycleNumber, cycle > 0 && click === 0 ? "dismiss_result" : "request_ten");
        await domClick(token, macro.ten_times_point || [0.66, 0.84]);
        await domDelay(macro.click_delay_seconds || 1.2, BLESSING_SPEED_FACTOR);
      }
      await domDelay(macro.confirm_delay_seconds || 1.5, BLESSING_SPEED_FACTOR);
      updateFlowContext("blessing", cycleNumber, "confirm_ok");
      await domClick(token, macro.ok_point || [0.70, 0.64]);
      updateFlowContext("blessing", cycleNumber, "result_wait");
      updateDomFlow("blessing", `Cầu phúc: ${cycleNumber} lượt`, "result_wait");
      if (cycleNumber === 1 || cycleNumber % 10 === 0) {
        appendDiagnostic("flow_progress", { flow: "blessing", cycle: cycleNumber, step: "result_wait" });
      }
      await domDelay(macro.result_delay_seconds || 3, BLESSING_SPEED_FACTOR);
      if (restEvery > 0 && cycleNumber % restEvery === 0) {
        updateFlowContext("blessing", cycleNumber, "periodic_rest");
        updateDomFlow("blessing", `Cầu phúc: nghỉ sau ${cycleNumber} lượt`, "periodic_rest");
        await domDelay(macro.rest_delay_seconds || 5, BLESSING_SPEED_FACTOR);
      }
    }
  }

  async function runCodeRedeem(token, macro) {
    const codes = expandCodes(macro);
    for (let index = 0; index < codes.length; index += 1) {
      updateDomFlow("code_redeem", `Mở NPC cho code ${index + 1}/${codes.length}`);
      await domClick(token, macro.npc_point || [0.288, 0.465]);
      await domDelay(macro.open_delay_seconds || 1);
      await domClick(token, macro.option_point || [0.50, 0.43]);
      await domDelay(macro.option_delay_seconds || 1);
      await domClick(token, macro.input_point || [0.50, 0.51]);
      await domClearAndType(token, codes[index]);
      await domClick(token, macro.submit_point || [0.50, 0.57]);
      await domDelay(macro.submit_delay_seconds || 1.4);
      await domPress(token, "Enter", "Enter", 13);
      await domDelay(macro.dismiss_delay_seconds || 0.8);
      await domPress(token, "Escape", "Escape", 27);
      await domDelay(macro.reopen_delay_seconds || 1);
      updateDomFlow("code_redeem", `Đổi code ${index + 1}/${codes.length}: ${codes[index]}`);
    }
  }

  async function runDiscardItems(token, macro) {
    const maxCycles = Number(macro.max_cycles || 0);
    for (let cycle = 0; maxCycles <= 0 || cycle < maxCycles; cycle += 1) {
      await domClick(token, macro.sort_point || [0.796, 0.919]);
      await domDelay(macro.sort_delay_seconds || 0.9);
      await domClick(token, macro.first_item_point || [0.342, 0.229]);
      await domDelay(macro.detail_delay_seconds || 0.7);
      await domClick(token, macro.discard_point || [0.710, 0.502]);
      await domDelay(macro.confirm_delay_seconds || 0.7);
      await domClick(token, macro.confirm_point || [0.685, 0.631]);
      updateDomFlow("discard_items", `Đã vứt: ${cycle + 1} vật phẩm`);
      await domDelay(macro.refresh_delay_seconds || 1);
    }
  }

  async function runUseInventoryItem(token, macro) {
    const maxCycles = Number(macro.max_cycles || 0);
    const itemSlot = macro.item_slot === "right" ? "right" : "left";
    const itemPoint = (macro.item_points || {})[itemSlot] || macro.item_point || [0.337, 0.207];
    for (let cycle = 0; maxCycles <= 0 || cycle < maxCycles; cycle += 1) {
      await domClick(token, itemPoint);
      await domDelay(macro.detail_delay_seconds || 0.7);
      await domClick(token, macro.use_point || [0.724, 0.345]);
      if (macro.confirm_point != null) {
        await domDelay(macro.confirm_delay_seconds || 0.7);
        await domClick(token, macro.confirm_point);
      }
      updateDomFlow("use_inventory_item", `Đã dùng ô ${itemSlot === "right" ? "phải" : "trái"}: ${cycle + 1}`);
      await domDelay(macro.refresh_delay_seconds || 1);
    }
  }

  async function runCoinShake(token, macro) {
    const maxCycles = Number(macro.max_cycles || 0);
    for (let cycle = 0; maxCycles <= 0 || cycle < maxCycles; cycle += 1) {
      await domClick(token, macro.shake_point || [0.585, 0.820]);
      await domDelay(macro.confirm_delay_seconds || 0.7);
      await domClick(token, macro.confirm_point || [0.685, 0.631]);
      updateDomFlow("coin_shake", `Rung xu: ${cycle + 1} lượt`);
      await domDelay(macro.result_delay_seconds || 1.2);
    }
  }

  async function runAutoAttack(token, macro) {
    const attackPoint = macro.attack_point || [0.927, 0.822];
    const skillPoints = macro.skill_points || [
      [0.927, 0.517], [0.853, 0.566], [0.799, 0.670], [0.875, 0.710],
      [0.927, 0.653], [0.774, 0.820], [0.845, 0.820],
    ];
    const maxCycles = Number(macro.max_cycles || 0);
    for (let cycle = 0; maxCycles <= 0 || cycle < maxCycles; cycle += 1) {
      await domClick(token, attackPoint);
      await domDelay(macro.button_delay_seconds || 0.18);
      for (const point of skillPoints) {
        await domClick(token, point);
        await domDelay(macro.button_delay_seconds || 0.18);
      }
      updateDomFlow("auto_attack", `Tự động đánh: ${cycle + 1} vòng`);
      await domDelay(macro.round_delay_seconds || 0.3);
    }
  }

  async function startDomFlow(flow, macro) {
    if (domToken && !domToken.cancelled) throw new Error("Một flow DOM khác đang chạy");
    const controller = await api("/status");
    if (controller.diagnosticsVersion !== 1) {
      throw new Error("Hãy restart start-extension-server.ps1 để bật log chẩn đoán");
    }
    if (controller.state === "running") throw new Error("Một flow Python khác đang chạy");
    localStorage.removeItem(NETWORK_EVENT_KEY);
    localStorage.setItem(FLOW_CONTEXT_KEY, JSON.stringify({
      flow, cycle: 0, step: "starting", startedAt: Date.now(), updatedAt: Date.now(),
    }));
    appendDiagnostic("flow_start", { flow });
    const token = { cancelled: false };
    domToken = token;
    updateDomFlow(flow, `Đang chạy không-CDP: ${flow}`);
    void (async () => {
      try {
        if (flow === "blessing") await runBlessing(token, macro);
        else if (flow === "code_redeem") await runCodeRedeem(token, macro);
        else if (flow === "discard_items") await runDiscardItems(token, macro);
        else if (flow === "use_inventory_item") await runUseInventoryItem(token, macro);
        else if (flow === "coin_shake") await runCoinShake(token, macro);
        else if (flow === "auto_attack") await runAutoAttack(token, macro);
        else throw new Error(`Flow DOM chưa hỗ trợ: ${flow}`);
        domFlow = { state: "done", flow, message: "Flow hoàn tất" };
      } catch (error) {
        domFlow = token.cancelled || error.message === "FLOW_STOPPED"
          ? { state: "stopped", flow, message: "Đã dừng flow" }
          : { state: "error", flow, message: error.message };
      } finally {
        token.cancelled = true;
        appendDiagnostic("flow_end", { flow, state: domFlow.state, message: domFlow.message });
        if (domToken === token) domToken = null;
        showStatus(domFlow);
      }
    })();
    return domFlow;
  }

  async function runFlow(flow, macroOverrides = {}) {
    try {
      let result;
      if (DOM_RUNNERS.has(flow.runner)) {
        const config = await api(`/macro?id=${encodeURIComponent(flow.id)}`);
        result = await startDomFlow(flow.id, { ...config.macro, ...macroOverrides });
      } else {
        domFlow = { state: "idle", message: "Sẵn sàng" };
        localStorage.setItem(FLOW_CONTEXT_KEY, JSON.stringify({
          flow: flow.id, cycle: 0, step: "python_os_input",
          startedAt: Date.now(), updatedAt: Date.now(),
        }));
        appendDiagnostic("flow_start", { flow: flow.id, step: "python_os_input" });
        result = await api("/run", { method: "POST", body: JSON.stringify({ flow: flow.id }) });
      }
      showStatus(result);
    } catch (error) {
      showStatus({ state: "error", message: error.message });
    }
  }

  async function stopFlow() {
    try {
      let result;
      if (domToken && !domToken.cancelled) {
        domToken.cancelled = true;
        domFlow = { state: "stopping", flow: domFlow.flow, message: "Đang dừng flow" };
        result = domFlow;
      } else {
        result = await api("/stop", { method: "POST", body: "{}" });
      }
      showStatus(result);
    } catch (error) {
      showStatus({ state: "error", message: error.message });
    }
  }

  async function refresh() {
    try {
      if (!flows.length) flows = (await api("/flows")).flows;
      const controllerStatus = await api("/status");
      const status = domFlow.state !== "idle" ? domFlow : controllerStatus;
      setConnected(true);
      if (controllerStatus.diagnosticsVersion !== 1) {
        showStatus({ state: "error", message: "Controller cũ: hãy restart để bật log chẩn đoán" });
        return;
      }
      await reportQueuedNetworkEvents();
      const networkEvent = recentNetworkEvent();
      const networkMessage = status.state !== "running" ? networkEventMessage(networkEvent) : "";
      if (networkMessage) {
        showStatus({ state: "error", message: networkMessage });
      } else {
        showStatus(status);
      }
    } catch (_) {
      setConnected(false);
      if (domToken && !domToken.cancelled) {
        showStatus(domFlow);
        return;
      }
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
  watchGameGuard();
  refresh();
  setInterval(refresh, 1200);
})();
