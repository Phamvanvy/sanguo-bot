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
    "warehouse_take_loop",
    "auto_attack_loop",
    "star_reappraisal_loop",
    "mount_skill_learn_once",
    "dungeon_route",
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
  let timerKeepAlive = null;
  // Live game state (map, our position, live monsters) decoded by
  // network_probe.js from the game's own WebSocket traffic.
  let latestWorld = null;
  // When the map id last really changed. The probe also flags entering the
  // same map again (the game re-sends it, e.g. in Hà Đông), which is no portal.
  let mapSwitchedAt = 0;
  let knownMapId = null;
  window.addEventListener("message", (event) => {
    if (event.source !== window || event.data?.source !== "sanguo-world") return;
    latestWorld = event.data;
    const mapId = latestWorld.mapId;
    if (mapId != null && mapId !== knownMapId) {
      if (knownMapId != null) mapSwitchedAt = Date.now();
      knownMapId = mapId;
    }
  });

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

  async function startTimerKeepAlive() {
    if (timerKeepAlive || typeof RTCPeerConnection !== "function") return;
    const left = new RTCPeerConnection({ iceServers: [] });
    const right = new RTCPeerConnection({ iceServers: [] });
    const leftCandidates = [];
    const rightCandidates = [];
    const outbound = left.createDataChannel("sanguo-background-timer");
    const state = { left, right, outbound, inbound: null };
    timerKeepAlive = state;
    right.addEventListener("datachannel", (event) => { state.inbound = event.channel; });
    outbound.addEventListener("open", () => appendDiagnostic("timer_keepalive_open"));
    left.addEventListener("icecandidate", (event) => {
      if (!event.candidate) return;
      if (right.remoteDescription) void right.addIceCandidate(event.candidate).catch(() => {});
      else leftCandidates.push(event.candidate);
    });
    right.addEventListener("icecandidate", (event) => {
      if (!event.candidate) return;
      if (left.remoteDescription) void left.addIceCandidate(event.candidate).catch(() => {});
      else rightCandidates.push(event.candidate);
    });
    try {
      await left.setLocalDescription(await left.createOffer());
      await right.setRemoteDescription(left.localDescription);
      for (const candidate of leftCandidates) await right.addIceCandidate(candidate);
      await right.setLocalDescription(await right.createAnswer());
      await left.setRemoteDescription(right.localDescription);
      for (const candidate of rightCandidates) await left.addIceCandidate(candidate);
    } catch (error) {
      appendDiagnostic("timer_keepalive_error", { message: String(error?.message || error) });
      stopTimerKeepAlive();
    }
  }

  function stopTimerKeepAlive() {
    if (!timerKeepAlive) return;
    const state = timerKeepAlive;
    timerKeepAlive = null;
    for (const resource of [state.inbound, state.outbound, state.left, state.right]) {
      try { resource?.close(); } catch (_) { /* Already closed. */ }
    }
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
    browserProcess: navigator.userAgent.includes("Edg/")
      ? "msedge.exe"
      : (navigator.brave ? "brave.exe" : "chrome.exe"),
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
        const itemOptions = [
          { label: "Ô trái", overrides: { item_slot: "left" } },
          { label: "Ô phải", overrides: { item_slot: "right" } },
          {
            label: "Ô trái ×99 + sắp xếp",
            overrides: { item_slot: "left", auto_sort_batches: true },
            wide: true,
          },
        ];
        for (const option of itemOptions) {
          const button = document.createElement("button");
          button.type = "button";
          button.disabled = running;
          button.textContent = option.label;
          button.classList.toggle("sg-wide", Boolean(option.wide));
          button.addEventListener("click", () => runFlow(flow, option.overrides));
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
    let events = [];
    try {
      events = JSON.parse(localStorage.getItem(NETWORK_QUEUE_KEY) || "[]");
      const lastEvent = JSON.parse(localStorage.getItem(NETWORK_EVENT_KEY) || "null");
      if (lastEvent) events.push(lastEvent);
    } catch (_) { /* Diagnostics must not break input when storage is unavailable. */ }
    const socketEvent = events
      .filter((event) => (
        ["ws_open", "ws_close"].includes(event.type)
        && Number(event.at || 0) >= Number(token.startedAt || 0)
      ))
      .sort((left, right) => Number(right.at || 0) - Number(left.at || 0))[0];
    if (socketEvent?.type === "ws_close") {
      const reason = socketEvent.reason ? ` (${socketEvent.reason})` : "";
      throw new Error(`WebSocket bị đóng: code ${socketEvent.code}${reason}. Flow đã tự dừng.`);
    }
  }

  function updateDomFlow(flow, message, step = "status") {
    domFlow = { state: "running", flow, message };
    const cycle = Number(message.match(/\d+/)?.[0] || 0);
    updateFlowContext(flow, cycle, step);
    showStatus(domFlow);
  }

  // Points are fractions of the game canvas. When the tab is wider than the
  // canvas (e.g. Brave's info bar steals height) the game is letterboxed, so
  // map onto #screen itself; when it fills the tab this equals the viewport.
  function eventTargetAt(point) {
    const rect = document.querySelector("#screen")?.getBoundingClientRect();
    const box = rect && rect.width > 0 && rect.height > 0
      ? rect
      : { left: 0, top: 0, width: innerWidth, height: innerHeight };
    const x = box.left + Number(point[0]) * box.width;
    const y = box.top + Number(point[1]) * box.height;
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
    // Anything but the canvas is plain HTML the game lays over it - the
    // "S.lượng nhập vào" box is a real dialog with a real input - and those
    // buttons only act on a click event (user, 2026-09-18: "Đồng ý" never
    // fired). The canvas still must not get one, see above.
    if (target.id !== "screen") dispatchMouse(target, "click", x, y, 0);
    return { target, x, y };
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

  async function domHtmlClick(token, point) {
    ensureDomActive(token);
    const { target } = eventTargetAt(point);
    const clickable = target.closest?.('button, [role="button"], input[type="button"], input[type="submit"]') || target;
    if (typeof clickable.focus === "function") clickable.focus({ preventScroll: true });
    if (typeof clickable.click !== "function") throw new Error("Không tìm thấy nút HTML để bấm");
    clickable.click();
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

  async function runCodeRedeem(token, macro, flow = "code_redeem") {
    const codes = expandCodes(macro);
    if (!codes.length) throw new Error("Chưa cấu hình code để nhập");
    const maxCycles = Number(macro.max_cycles ?? 1);
    for (let cycle = 0; maxCycles <= 0 || cycle < maxCycles; cycle += 1) {
      for (let index = 0; index < codes.length; index += 1) {
        const attempt = cycle * codes.length + index + 1;
        if (macro.map_point != null) {
          updateFlowContext(flow, attempt, "open_map");
          await domClick(token, macro.map_point);
          await domDelay(macro.map_delay_seconds || 1.2);
        }
        updateDomFlow(flow, `Mở NPC để nhập ${codes[index]} lần ${attempt}`);
        await domClick(token, macro.npc_point || [0.288, 0.465]);
        await domDelay(macro.open_delay_seconds || 1);
        await domClick(token, macro.option_point || [0.50, 0.43]);
        await domDelay(macro.option_delay_seconds || 1);
        await domClick(token, macro.input_point || [0.50, 0.51]);
        await domClearAndType(token, codes[index]);
        await domHtmlClick(token, macro.submit_point || [0.494, 0.556]);
        await domDelay(macro.submit_delay_seconds || 1.2);
        await domClick(token, macro.notification_point || [0.500, 0.518]);
        await domDelay(macro.dismiss_delay_seconds || 0.8);
        await domDelay(macro.reopen_delay_seconds || 1);
        updateDomFlow(flow, `Đã nhập ${codes[index]}: ${attempt} lần`);
      }
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
    const batchSize = Math.max(1, Number(macro.max_cycles ?? 99));
    const autoSortBatches = Boolean(macro.auto_sort_batches);
    const maxBatches = autoSortBatches ? Number(macro.max_batches ?? 0) : 1;
    const itemSlot = macro.item_slot === "right" ? "right" : "left";
    const itemPoint = (macro.item_points || {})[itemSlot] || macro.item_point || [0.337, 0.207];
    for (let batch = 0; maxBatches <= 0 || batch < maxBatches; batch += 1) {
      for (let cycle = 0; cycle < batchSize; cycle += 1) {
        await domClick(token, itemPoint);
        await domDelay(macro.detail_delay_seconds || 0.7);
        await domClick(token, macro.use_point || [0.724, 0.345]);
        if (macro.confirm_point != null) {
          await domDelay(macro.confirm_delay_seconds || 0.7);
          await domClick(token, macro.confirm_point);
        }
        const total = batch * batchSize + cycle + 1;
        updateDomFlow("use_inventory_item", `Đã dùng ô ${itemSlot === "right" ? "phải" : "trái"}: ${total}`);
        await domDelay(macro.refresh_delay_seconds || 1);
      }
      if (!autoSortBatches) break;
      updateFlowContext("use_inventory_item", batch + 1, "sort_next_batch");
      await domClick(token, macro.batch_sort_point || [0.813, 0.917]);
      updateDomFlow("use_inventory_item", `Đã sắp xếp sau batch ${batch + 1} × ${batchSize}`, "sort_next_batch");
      await domDelay(macro.batch_sort_delay_seconds || 1);
    }
  }

  // Kho đang mở: lấy ô đầu tiên vào hành trang (hộp số lượng game điền sẵn tối
  // đa), rồi "Sắp xếp kho hàng" để món kế dồn lên ô đầu, lặp tới khi Stop.
  async function runWarehouseTake(token, macro) {
    const maxCycles = Number(macro.max_cycles || 0);
    for (let cycle = 0; maxCycles <= 0 || cycle < maxCycles; cycle += 1) {
      await domClick(token, macro.first_slot_point || [0.130, 0.270]);
      await domDelay(macro.detail_delay_seconds || 0.7);
      await domClick(token, macro.take_point || [0.724, 0.345]);
      if (macro.amount_confirm_point != null) {
        await domDelay(macro.amount_delay_seconds || 0.7);
        await domClick(token, macro.amount_confirm_point);
      }
      await domDelay(macro.refresh_delay_seconds || 1);
      await domClick(token, macro.sort_point || [0.421, 0.897]);
      updateDomFlow("warehouse_take", `Đã lấy từ kho: ${cycle + 1} ô`);
      await domDelay(macro.sort_delay_seconds || 1);
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

  async function runStarReappraisal(token, macro) {
    const maxCycles = Number(macro.max_cycles || 0);
    for (let cycle = 0; maxCycles <= 0 || cycle < maxCycles; cycle += 1) {
      const cycleNumber = cycle + 1;
      updateFlowContext("star_reappraisal", cycleNumber, "open_star_menu");
      await domClick(token, macro.star_button_point || [0.227, 0.869]);
      await domDelay(macro.menu_delay_seconds || 0.8);
      updateFlowContext("star_reappraisal", cycleNumber, "reappraise");
      await domClick(token, macro.reappraise_point || [0.498, 0.756]);
      await domDelay(macro.result_delay_seconds || 1.2);
      updateFlowContext("star_reappraisal", cycleNumber, "confirm_result");
      await domClick(token, macro.confirm_point || [0.499, 0.693]);
      updateDomFlow("star_reappraisal", `Giám định lại cấp sao: ${cycleNumber} lượt`);
      await domDelay(macro.next_cycle_delay_seconds || 0.8);
    }
  }

  async function runMountSkillLearnOnce(token, macro) {
    updateFlowContext("mount_skill_learn", 1, "select_book");
    await domClick(token, macro.book_point || [0.289, 0.807]);
    await domDelay(macro.detail_delay_seconds || 1.0);
    updateFlowContext("mount_skill_learn", 1, "learn_skill");
    await domClick(token, macro.learn_point || [0.703, 0.224]);
    await domDelay(macro.confirm_delay_seconds || 0.8);
    updateFlowContext("mount_skill_learn", 1, "confirm");
    await domClick(token, macro.confirm_point || [0.696, 0.628]);
    updateDomFlow("mount_skill_learn", "Đã học kỹ năng thú cưỡi 1 lần", "done");
    await domDelay(macro.result_delay_seconds || 1.0);
  }

  async function runAutoAttack(token, macro) {
    const maxCycles = Number(macro.max_cycles || 0);
    for (let cycle = 0; maxCycles <= 0 || cycle < maxCycles; cycle += 1) {
      await attackRound(token, macro);
      updateDomFlow("auto_attack", `Tự động đánh: ${cycle + 1} vòng`);
      await domDelay(macro.round_delay_seconds || 0.3);
    }
  }

  async function attackRound(token, macro) {
    const attackPoint = macro.attack_point || [0.927, 0.822];
    const skillPoints = macro.skill_points || [
      [0.927, 0.517], [0.853, 0.566], [0.799, 0.670], [0.875, 0.710],
      [0.927, 0.653], [0.774, 0.820], [0.845, 0.820],
    ];
    await domClick(token, attackPoint);
    await domDelay(macro.button_delay_seconds || 0.18);
    for (const point of skillPoints) {
      await domClick(token, point);
      await domDelay(macro.button_delay_seconds || 0.18);
    }
  }

  // Real-time wait that still honours Stop; walks and portal loads are timed.
  async function domWait(token, seconds) {
    const deadline = Date.now() + Number(seconds) * 1000;
    while (Date.now() < deadline) {
      ensureDomActive(token);
      await new Promise((resolve) => setTimeout(resolve, Math.min(500, deadline - Date.now())));
    }
    ensureDomActive(token);
  }

  // A dungeon is a fixed route: walk by clicking the Map panel (the game
  // auto-paths) - to a coordinate (goto) or a fixed panel point (map_point) -
  // then fight until no monster is attacking. Portals stay locked while
  // monsters attack us, so a portal that does not take us means: fight, retry.
  // "canvas#screen@1592,200" - shows whether a click reached the game canvas
  // or landed on something else, e.g. the flow panel covering that spot.
  function describeClick({ target, x, y }) {
    const name = `${target.tagName?.toLowerCase() || "?"}${target.id ? `#${target.id}` : ""}`;
    return `${name}@${Math.round(x)},${Math.round(y)}`;
  }

  // Every step is logged to logs/extension-network.log (types dungeon_*),
  // so a route that drifts can be traced to the step and click that missed.
  async function runDungeonRoute(token, macro, flow) {
    // Started inside the dungeon (e.g. after a stop): the entry is behind us.
    const entryMap = macro.entry_map == null ? null : Number(macro.entry_map);
    const inside = entryMap != null && latestWorld?.mapId != null && latestWorld.mapId !== entryMap;
    const steps = [...(inside ? [] : macro.entry_steps || []), ...(macro.route_steps || [])];
    if (!steps.length) throw new Error("Chưa cấu hình đường đi phó bản");
    const unclosed = steps.find((step) => step.map_point && !step.map_close_point);
    if (unclosed) {
      throw new Error(`Bước "${unclosed.label}" mở Map nhưng thiếu map_close_point (nút X) để đóng lại`);
    }
    const unsized = steps.find((step) => step.goto && !step.map_size);
    if (unsized) {
      throw new Error(`Bước "${unsized.label}" đi theo tọa độ nhưng thiếu map_size (cỡ map, pixel)`);
    }
    const canvas = document.querySelector("#screen")?.getBoundingClientRect();
    appendDiagnostic("dungeon_start", {
      flow,
      message: `${steps.length} bước; viewport ${innerWidth}x${innerHeight}; `
        + (canvas
          ? `canvas ${Math.round(canvas.left)},${Math.round(canvas.top)} ${Math.round(canvas.width)}x${Math.round(canvas.height)}`
          : "không thấy canvas #screen")
        + `; dữ liệu game: ${describeWorld()}`
        + (inside ? "; đã ở trong phó bản nên bỏ qua bước vào cửa" : ""),
    });
    // Newest map switch the route has accounted for; a newer one before a
    // portal step means that portal was already crossed.
    let mapMark = mapSwitchedAt;
    for (const [index, step] of steps.entries()) {
      const title = `Bước ${index + 1}/${steps.length}: ${step.label || "đi tiếp"}`;
      const { label: _label, ...plan } = step;
      const stepStartedAt = Date.now();
      const clicks = [];
      const notes = [];
      updateDomFlow(flow, title, `step_${index + 1}`);
      appendDiagnostic("dungeon_step", { flow, message: `${title} ${JSON.stringify(plan)}; ${describeWorld()}` });
      const crossed = Boolean(step.portal) && mapSwitchedAt > mapMark;
      if (crossed) notes.push(`đã sang map ${latestWorld.mapId ?? "?"} từ trước nên bỏ qua`);
      for (let attempt = 0; !crossed && (step.goto || step.map_point || step.click_point); attempt += 1) {
        const since = Date.now();
        let walk;
        if (step.goto) {
          walk = await walkToCoord(token, macro, step, clicks);
        } else {
          if (step.map_point) notes.push(`đóng map: ${await walkByMap(token, macro, step, clicks)}`);
          else clicks.push(await domClick(token, step.click_point));
          walk = await waitForArrival(token, macro, step, since);
        }
        notes.push(walk.note);
        // A portal that did not change the map is still shut: monsters are
        // attacking us. So is an ambush on the way. Clear them, then retry.
        const shut = walk.how === "stuck"
          || (step.portal && ["timeout", "blocked"].includes(walk.how) && latestWorld?.frames);
        if (walk.how !== "ambush" && !shut) break;
        notes.push(describeFight(await fightUntilClear(token, macro, macro.ambush_fight_seconds || 60)));
        appendDiagnostic("dungeon_ambush", { flow, message: `${title}: ${notes.slice(-2).join(" → ")}` });
        if (attempt >= Number(macro.max_rewalks ?? 3)) {
          if (shut) throw new Error(`${title}: vẫn chưa qua được cổng sau ${attempt + 1} lần`);
          break;
        }
      }
      if (step.portal) mapMark = Math.max(mapMark, mapSwitchedAt);
      if (step.fight_seconds) {
        const until = step.ignore_idle_monsters ? "không còn quái đánh mình" : "hết quái";
        updateDomFlow(flow, `${title} (đánh tới khi ${until})`, `step_${index + 1}`);
        // One step can serve two versions of a dungeon (Cổ Mộ dễ and khó share
        // this route), and the hard one's last boss simply has more health, so
        // its cap is per map id (user, 2026-09-19).
        const seconds = (step.fight_seconds_by_map || {})[latestWorld?.mapId] ?? step.fight_seconds;
        notes.push(describeFight(await fightUntilClear(token, macro, seconds, {
          idle: !step.ignore_idle_monsters,
        })));
      }
      if (step.after_seconds) await domWait(token, step.after_seconds);
      const seconds = ((Date.now() - stepStartedAt) / 1000).toFixed(1);
      appendDiagnostic("dungeon_step_done", {
        flow,
        message: `${title} xong sau ${seconds}s; `
          + notes.map((note) => `${note}; `).join("")
          + `click: ${clicks.map(describeClick).join(" | ") || "không"}`,
      });
    }
    updateDomFlow(flow, `Đã chạy xong ${steps.length} bước phó bản`, "done");
    if (macro.finish_chime !== false) playChime();
  }

  // A bell when a dungeon is done, for a user away from the screen (user,
  // 2026-09-14). Synthesised with Web Audio: no sound file to ship.
  function playChime() {
    try {
      const audio = new AudioContext();
      audio.resume();
      const start = audio.currentTime + 0.05;
      const notes = [[0, 880], [0.3, 1175], [0.6, 1568], [1.5, 880], [1.8, 1175], [2.1, 1568]];
      for (const [at, hz] of notes) {
        const tone = audio.createOscillator();
        const gain = audio.createGain();
        tone.frequency.value = hz;
        gain.gain.setValueAtTime(0.0001, start + at);
        gain.gain.exponentialRampToValueAtTime(0.35, start + at + 0.02);
        gain.gain.exponentialRampToValueAtTime(0.0001, start + at + 1.2);
        tone.connect(gain).connect(audio.destination);
        tone.start(start + at);
        tone.stop(start + at + 1.25);
      }
      setTimeout(() => audio.close(), 4500);
    } catch (_) { /* No audio: the panel still shows the route as done. */ }
  }

  // Opens Map, picks the destination and closes Map again: the panel never
  // closes by itself and would swallow every later click.
  async function walkByMap(token, macro, step, clicks) {
    clicks.push(await domClick(token, macro.map_button_point || [0.85, 0.07]));
    await domWait(token, macro.map_open_delay_seconds || 1.2);
    clicks.push(await domClick(token, step.map_point));
    await domWait(token, macro.map_close_delay_seconds || 0.6);
    // The game's close key does not reach the Map from here: always its X button.
    clicks.push(await domClick(token, step.map_close_point));
    return "nút X";
  }

  // Where the Map panel draws a map, in canvas fractions. It draws the map 1:1
  // in the game's 1280x640 logical screen, centred on (640, 304) and at most
  // 1124x484; a bigger map opens scrolled to show us about 50 below the middle
  // (8 Thiên Long legs put us 292 +-16 px below its top edge), clamped at the
  // map's edges. Its scroll arrows do not scroll it when clicked from here
  // (Thiên Long, 2026-09-14), so only what shows as it opens can be clicked.
  // Its X sits 34 right of and 20 above the drawn map. Fitted on the X buttons
  // of maps 768, 1137, 1140, 1141, Hà Đông ngoài, Thiên Long trận and its
  // lobby, and on portals / our marker in the user's screenshots.
  function mapPanel(macro, size, me) {
    const [screenW, screenH] = (macro.map_panel_screen || [1280, 640]).map(Number);
    const [centerX, centerY] = (macro.map_panel_center || [640, 304]).map(Number);
    const [maxW, maxH] = (macro.map_panel_max_size || [1124, 484]).map(Number);
    const [followX, followY] = (macro.map_panel_follow_offset || [0, 50]).map(Number);
    // Room kept off an edge the scroll cuts: the scroll rule is off by ~16 px.
    const margin = Number(macro.map_panel_margin || 48);
    const [mapW, mapH] = size.map(Number);
    const view = { x: Math.min(mapW, maxW), y: Math.min(mapH, maxH) };
    const map = { x: mapW, y: mapH };
    const follow = { x: followX, y: followY };
    const origin = { x: centerX - view.x / 2, y: centerY - view.y / 2 };
    const scroll = {};
    for (const axis of ["x", "y"]) {
      const wanted = me[axis] - view[axis] / 2 - follow[axis];
      scroll[axis] = Math.min(Math.max(wanted, 0), map[axis] - view[axis]);
    }
    const fraction = (x, y) => [x / screenW, y / screenH];
    return {
      scroll,
      // Drawn on the Map as it opens, clear of any edge the scroll cuts off.
      shows(point) {
        return ["x", "y"].every((axis) => {
          const low = scroll[axis] > 0 ? scroll[axis] + margin : 0;
          const high = scroll[axis] + view[axis] < map[axis] ? scroll[axis] + view[axis] - margin : map[axis];
          return point[axis] >= low && point[axis] <= high;
        });
      },
      toFraction: (point) => fraction(origin.x + point.x - scroll.x, origin.y + point.y - scroll.y),
      closePoint: fraction(origin.x + view.x + 34, origin.y - 20),
    };
  }

  // Walkable path tiles per map id: walk_grids.js, generated from the game's
  // own map data by tools/assets/walk_grid.mjs, loads before this script. One
  // tile is one game coordinate.
  const decodedGrids = new Map();
  function walkGridFor(mapId) {
    const source = globalThis.SANGUO_WALK_GRIDS?.[mapId];
    if (!source) return null;
    if (!decodedGrids.has(mapId)) {
      const bits = Uint8Array.from(atob(source.blocked), (char) => char.charCodeAt(0));
      const blocked = (index) => (bits[index >> 3] >> (index & 7)) & 1;
      decodedGrids.set(mapId, {
        ...source,
        open: (x, y) => x >= 0 && y >= 0 && x < source.w && y < source.h && !blocked(y * source.w + x),
      });
    }
    return decodedGrids.get(mapId);
  }

  // Shortest walkable path (8 directions, never cutting a blocked corner) from
  // `start` to a tile within `within` of `goal`; tiles start to end, or null.
  function findPath(grid, start, goal, within = 0) {
    const { w, h } = grid;
    const cost = new Float64Array(w * h).fill(Infinity);
    const came = new Int32Array(w * h).fill(-1);
    const heap = [];
    const swap = (a, b) => { [heap[a], heap[b]] = [heap[b], heap[a]]; };
    const push = (entry) => {
      heap.push(entry);
      for (let i = heap.length - 1; i > 0 && heap[(i - 1) >> 1][0] > heap[i][0]; i = (i - 1) >> 1) swap(i, (i - 1) >> 1);
    };
    const pop = () => {
      const top = heap[0];
      const last = heap.pop();
      if (heap.length) {
        heap[0] = last;
        for (let i = 0; ;) {
          const [left, right] = [2 * i + 1, 2 * i + 2];
          let least = i;
          if (left < heap.length && heap[left][0] < heap[least][0]) least = left;
          if (right < heap.length && heap[right][0] < heap[least][0]) least = right;
          if (least === i) break;
          swap(i, least);
          i = least;
        }
      }
      return top;
    };
    const guess = (x, y) => Math.max(0, Math.hypot(goal.x - x, goal.y - y) - within);
    cost[start.y * w + start.x] = 0;
    push([guess(start.x, start.y), start.y * w + start.x]);
    while (heap.length) {
      const [, index] = pop();
      const x = index % w;
      const y = (index - x) / w;
      if (Math.max(Math.abs(x - goal.x), Math.abs(y - goal.y)) <= within) {
        const path = [];
        for (let at = index; at !== -1; at = came[at]) path.push({ x: at % w, y: Math.floor(at / w) });
        return path.reverse();
      }
      for (let dy = -1; dy <= 1; dy += 1) {
        for (let dx = -1; dx <= 1; dx += 1) {
          if ((!dx && !dy) || !grid.open(x + dx, y + dy)) continue;
          if (dx && dy && (!grid.open(x + dx, y) || !grid.open(x, y + dy))) continue;
          const next = (y + dy) * w + x + dx;
          const nextCost = cost[index] + (dx && dy ? Math.SQRT2 : 1);
          if (nextCost >= cost[next]) continue;
          cost[next] = nextCost;
          came[next] = index;
          push([nextCost + guess(x + dx, y + dy), next]);
        }
      }
    }
    return null;
  }

  // Steps on foot from `from` to every tile up to `limit` steps away (8
  // directions, never cutting a blocked corner); Infinity past that.
  function walkDistances(grid, from, limit) {
    const steps = new Float64Array(grid.w * grid.h).fill(Infinity);
    const start = grid.open(from.x, from.y) ? from : nearestOpen(grid, from, 2);
    if (!start) return steps;
    const queue = [start.y * grid.w + start.x];
    steps[queue[0]] = 0;
    for (let head = 0; head < queue.length; head += 1) {
      const index = queue[head];
      if (steps[index] >= limit) continue;
      const x = index % grid.w;
      const y = (index - x) / grid.w;
      for (let dy = -1; dy <= 1; dy += 1) {
        for (let dx = -1; dx <= 1; dx += 1) {
          if ((!dx && !dy) || !grid.open(x + dx, y + dy)) continue;
          if (dx && dy && (!grid.open(x + dx, y) || !grid.open(x, y + dy))) continue;
          const next = (y + dy) * grid.w + x + dx;
          if (steps[next] <= steps[index] + 1) continue;
          steps[next] = steps[index] + 1;
          queue.push(next);
        }
      }
    }
    return steps;
  }

  // Nearest open tile to `tile` (itself when open), within `radius` tiles.
  function nearestOpen(grid, tile, radius) {
    for (let ring = 0; ring <= radius; ring += 1) {
      let best = null;
      for (let dy = -ring; dy <= ring; dy += 1) {
        for (let dx = -ring; dx <= ring; dx += 1) {
          if (Math.max(Math.abs(dx), Math.abs(dy)) !== ring || !grid.open(tile.x + dx, tile.y + dy)) continue;
          if (!best || Math.hypot(dx, dy) < best.d) best = { x: tile.x + dx, y: tile.y + dy, d: Math.hypot(dx, dy) };
        }
      }
      if (best) return { x: best.x, y: best.y };
    }
    return null;
  }

  function straightLine(start, goal) {
    const steps = Math.max(Math.abs(goal.x - start.x), Math.abs(goal.y - start.y), 1);
    return Array.from({ length: steps + 1 }, (_, i) => ({
      x: Math.round(start.x + ((goal.x - start.x) * i) / steps),
      y: Math.round(start.y + ((goal.y - start.y) * i) / steps),
    }));
  }

  // The way from `start` to `goal` in tiles: to the goal itself, else to the
  // nearest tile within `tolerance` (a boss on an altar), else within 6.
  // Without a grid for the map: a straight line, and the game finds the rest.
  function planRoute(grid, start, goal, tolerance) {
    if (!grid) return straightLine(start, goal);
    const from = grid.open(start.x, start.y) ? start : nearestOpen(grid, start, 3);
    if (!from) return null;
    return findPath(grid, from, goal, 0) || findPath(grid, from, goal, tolerance) || findPath(grid, from, goal, 6);
  }

  async function currentPosition(token) {
    for (let waited = 0; !latestWorld?.me && waited < 6; waited += 0.5) await domWait(token, 0.5);
    if (!latestWorld?.me) {
      throw new Error(`Chưa biết vị trí nhân vật (${describeWorld()}); đi 1 bước bằng tay rồi chạy lại`);
    }
    return { ...latestWorld.me };
  }

  // Door tiles to try for a portal step: the coordinate the user gave, exactly,
  // then any exit the game data puts within 3 tiles of it.
  function doorTiles(grid, goal) {
    const tiles = [goal];
    for (const [x, y] of grid?.exits || []) {
      const near = Math.max(Math.abs(x - goal.x), Math.abs(y - goal.y)) <= 3;
      if (near && (x !== goal.x || y !== goal.y)) tiles.push({ x, y });
    }
    return tiles;
  }

  // Per door, the tile we last walked in from: a door that did not take us is
  // left back that way and walked into again (not round to its far side).
  const doorApproach = new Map();

  // Walks to a coordinate as the game prints it next to the map name (1 unit =
  // coord_unit map pixels = one path tile). Plans the way on the map's walk
  // grid, then each leg opens the Map and clicks the farthest tile of that way
  // the Map shows, so the game's own pathfinder always gets a spot it can
  // reach. A boss spot counts as reached within goto_tolerance tiles; a door
  // only exactly (user, 2026-09-14). Every leg logs the tile it clicked and
  // where we ended up.
  async function walkToCoord(token, macro, step, clicks) {
    const unit = Number(macro.coord_unit || 8);
    const [cellX, cellY] = step.goto.map(Number);
    const tolerance = step.portal ? 0 : Number(step.goto_tolerance ?? macro.goto_tolerance ?? 1);
    // The game prints map pixels / unit rounded down.
    const tileOf = (point) => ({ x: Math.floor(point.x / unit), y: Math.floor(point.y / unit) });
    const center = (tile) => ({ x: (tile.x + 0.5) * unit, y: (tile.y + 0.5) * unit });
    const same = (a, b) => a.x === b.x && a.y === b.y;
    const reached = (point) => {
      const tile = tileOf(point);
      return Math.abs(tile.x - cellX) <= tolerance && Math.abs(tile.y - cellY) <= tolerance;
    };
    const where = (point) => `${tileOf(point).x},${tileOf(point).y}`;
    const maxLegs = Number(macro.goto_max_legs || 12);
    const cap = Number(macro.goto_max_correction || 32);
    const bias = { x: 0, y: 0 };
    const legs = [];
    let lastEnd = null;
    let stalls = 0;
    let shrink = 1;
    let doorIndex = 0;
    const summary = () => `đi tới ${step.goto.join(",")}: ${legs.join(" → ") || "đã đứng sẵn ở đó"}`;
    for (let leg = 0; leg < maxLegs; leg += 1) {
      const me = await currentPosition(token);
      if (!step.portal && reached(me)) {
        return { how: "arrived", note: `${summary()}; đứng ở ${where(me)}` };
      }
      const grid = walkGridFor(latestWorld?.mapId);
      const doors = step.portal ? doorTiles(grid, { x: cellX, y: cellY }) : null;
      const goal = doors ? doors[Math.min(doorIndex, doors.length - 1)] : { x: cellX, y: cellY };
      const doorKey = `${latestWorld?.mapId}:${goal.x},${goal.y}`;
      const panel = mapPanel(macro, grid ? grid.size : step.map_size, me);
      // Standing on the door and it did not take us: back out the way we came.
      const backOff = Boolean(doors) && leg === 0 && same(tileOf(me), goal);
      const route = backOff ? [tileOf(me)] : planRoute(grid, tileOf(me), goal, tolerance);
      if (!route) {
        return { how: "blocked", note: `không có đường tới ${goal.x},${goal.y} từ ${where(me)}; ${summary()}` };
      }
      let hop = route.length - 1;
      while (hop > 0 && !panel.shows(center(route[hop]))) hop -= 1;
      hop = Math.max(Math.min(1, route.length - 1), Math.floor(hop * shrink));
      let tile = route[hop];
      if (backOff) {
        const below = { x: goal.x, y: goal.y + 3 };
        tile = doorApproach.get(doorKey) || (grid && nearestOpen(grid, below, 2)) || below;
      }
      const final = hop === route.length - 1 && !backOff;
      if (doors && final && route.length > 4) doorApproach.set(doorKey, route[route.length - 4]);
      const point = backOff ? center(tile) : { x: center(tile).x + bias.x, y: center(tile).y + bias.y };
      const since = Date.now();
      await walkByMap(token, macro, {
        map_point: panel.toFraction(point),
        map_close_point: step.map_close_point || panel.closePoint,
      }, clicks);
      const walk = await waitForArrival(token, macro, {
        ...step,
        portal: Boolean(step.portal) && final,
        wait_seconds: step.wait_seconds ?? macro.goto_leg_seconds ?? 60,
      }, since, {
        idleSeconds: Number(macro.goto_idle_seconds || 5),
        settleSeconds: Number(macro.goto_settle_seconds || 2.5),
      });
      const after = latestWorld?.me || me;
      legs.push(`${tile.x},${tile.y}${final ? "" : " chặng"} ${walk.how} @${where(after)}`);
      if (walk.how === "map" || walk.how === "ambush") return { ...walk, note: `${walk.note}; ${summary()}` };
      // Walks that keep ending on the same spot short of the target: something
      // (a shut gate, monsters in the way) blocks it. Say so, stop clicking.
      const stalled = lastEnd && Math.hypot(after.x - lastEnd.x, after.y - lastEnd.y) <= 1.5 * unit;
      stalls = stalled && !["timeout", "idle"].includes(walk.how) ? stalls + 1 : 0;
      lastEnd = { ...after };
      const there = doors ? same(tileOf(after), goal) : reached(after);
      if (stalls >= 2 && !there) {
        // A door tile we cannot even stand on - something parked on it, or the
        // game refuses that last step - is not the end of it: the map data's
        // exit next to it is another way in (user, 2026-09-19: Cổ Mộ boss 1
        // kept stopping at 43,10, one tile short of 44,10, while the game's
        // own exit is 45,10, and every retry started over on 44,10).
        if (doors && doorIndex + 1 < doors.length) {
          doorIndex += 1;
          bias.x = 0;
          bias.y = 0;
          shrink = 1;
          stalls = 0;
          lastEnd = null;
          continue;
        }
        return { how: "blocked", note: `bị chặn ở ${where(after)}; ${summary()}` };
      }
      // Still walking when the leg timed out: plan again from here.
      if (backOff || walk.how === "timeout") continue;
      // Nothing moved: the click missed the tile onto something blocked. Aim nearer.
      if (walk.how === "idle") {
        bias.x = 0;
        bias.y = 0;
        shrink *= 0.6;
        if (shrink < 0.2) return { how: "blocked", note: `bấm mà không đi được; ${summary()}` };
        continue;
      }
      shrink = 1;
      const off = { x: center(tile).x - after.x, y: center(tile).y - after.y };
      if (doors && final && walk.how === "stuck" && same(tileOf(after), goal)) {
        // On this door tile and the map did not change: try the game data's
        // exit next to it; past the last one the door is shut (monsters).
        if (doorIndex + 1 < doors.length) {
          doorIndex += 1;
          bias.x = 0;
          bias.y = 0;
          continue;
        }
        return { ...walk, note: `${walk.note}; ${summary()}` };
      }
      // Not on the door tile yet: correct the click below and try it again.
      // Landing a little off the clicked tile is the Map model's error: aim
      // that much the other way next time. A big miss is a walk cut short.
      if (Math.hypot(off.x, off.y) <= 3 * unit) {
        bias.x = Math.max(-cap, Math.min(cap, bias.x + off.x));
        bias.y = Math.max(-cap, Math.min(cap, bias.y + off.y));
      }
    }
    return { how: "timeout", note: `chưa tới sau ${maxLegs} lượt; ${summary()}` };
  }

  // Attacks until no live monster is near us for a few checks in a row;
  // maxSeconds only caps a fight whose state cannot be read.
  async function fightUntilClear(token, macro, maxSeconds, { idle = true } = {}) {
    const startedAt = Date.now();
    const deadline = startedAt + Number(maxSeconds) * 1000;
    const needed = Math.max(1, Number(macro.clear_checks_needed || 3));
    let rounds = 0;
    let streak = 0;
    let last = null;
    // Monsters the game refuses to hit (ATTACK_FAIL): 14 "Mục tiêu không nằm
    // trong tầm nhìn", 8 "Mục tiêu không thể tấn công" (e.g. a Mật thư), 3/4
    // dead or gone. Leave them and move on (user, 2026-09-14).
    const unhittable = new Set([3, 4, 8, 14]);
    // The game refuses it ("Mục tiêu không nằm trong tầm nhìn"): leave it for
    // good and move on, hurt or not - a step is not worth grinding out (user,
    // 2026-09-17: Hà Đông 57,114 fought on for 218 s over monsters it could
    // not reach). Damage we land still holds the fight open, so a boss we ARE
    // hitting is never dropped this way.
    const unseen = new Set();
    // Nothing near loses health or dies for stall_giveup_seconds of Đánh: the
    // game cannot hit what is left from here - an idle statue that is no
    // target (Cổ Mộ 90,30, 103 rounds), one out of sight (Hà Đông 57,114, "Y
    // quan phản quân" 100% after 137 rounds). Only ones still at full health
    // are dropped: whatever we did hurt we can hit, weak damage just takes
    // longer (user, 2026-09-16). A boss waiting idle loses health as soon as
    // it is hit, so it stays in the fight too.
    const stallMs = Number(macro.stall_giveup_seconds ?? 30) * 1000;
    // Damage we land (network_probe.js reads it off the fight packets) keeps
    // the fight going for damage_grace_seconds even when the creature list is
    // empty: a Thiên Long boss never appears in it, so the step was calling
    // itself done while we were hitting the boss (user, 2026-09-16).
    const graceMs = Number(macro.damage_grace_seconds ?? 15) * 1000;
    const untouched = new Set();
    const joinedAt = new Map();
    let health = new Map();
    let progressAt = startedAt;
    let hitting = false;
    let landed = null;
    const refusals = new Map();          // ATTACK_FAIL reason -> how many targets
    const skipped = () => new Set([...untouched, ...unseen]);
    while (Date.now() < deadline) {
      if (macro.combat_check !== false) {
        for (const fail of latestWorld?.attackFails || []) {
          if (fail.at > startedAt && unhittable.has(fail.reason) && !unseen.has(fail.target)) {
            unseen.add(fail.target);
            refusals.set(fail.reason, (refusals.get(fail.reason) || 0) + 1);
          }
        }
        last = combatState(macro, skipped(), { idle });
        const now = Date.now();
        const near = last.near || [];
        const alive = new Set((latestWorld?.creatures || []).map(([id]) => id));
        const hurt = near.some((monster) => health.has(monster.id) && monster.hp < health.get(monster.id));
        const gone = [...health.keys()].some((id) => !alive.has(id));
        const blow = latestWorld?.hit;
        // Seen it die: the fight is over now, no need to sit out the grace.
        const killed = blow && (latestWorld?.kills || []).some((kill) => kill.id === blow.target
          && kill.at >= blow.at - 1000);
        hitting = Boolean(blow && !killed && blow.at > startedAt && now - blow.at <= graceMs);
        if (hitting) landed = blow;
        if (last.state !== "combat" || hurt || gone || hitting) progressAt = now;
        for (const monster of near) if (!joinedAt.has(monster.id)) joinedAt.set(monster.id, now);
        // Nothing near has lost health for stall_giveup_seconds: drop what is
        // still at FULL health (a statue that is no target, one behind a wall).
        // Whatever we did hurt stays in the fight - weak damage only takes
        // longer (user, 2026-09-16).
        const stalled = now - progressAt >= stallMs;
        const drop = stalled
          ? near.filter((monster) => monster.hp >= 200 && now - joinedAt.get(monster.id) >= stallMs)
          : [];
        if (drop.length) {
          for (const monster of drop) untouched.add(monster.id);
          last = combatState(macro, skipped(), { idle });
        }
        health = new Map((last.near || []).map((monster) => [monster.id, monster.hp]));
        streak = last.state === "clear" && !hitting ? streak + 1 : 0;
        if (streak >= needed) break;
        // No monster left: only wait out the checks. Pressing Đánh now picks
        // something that is no monster (the button turns into "Chat").
        if (last.state === "clear" && !hitting) {
          await domDelay(macro.round_delay_seconds || 0.4);
          continue;
        }
      }
      await attackRound(token, macro);
      rounds += 1;
      await domDelay(macro.round_delay_seconds || 0.4);
    }
    return {
      cleared: streak >= needed,
      rounds,
      seconds: (Date.now() - startedAt) / 1000,
      last,
      unseen: unseen.size,
      refusals: [...refusals].map(([reason, count]) => `${reason}×${count}`).join(" "),
      untouched: untouched.size,
      // Everything the game data still shows around us, however far: a fight
      // that ends with a boss alive says here whether the boss was in the data
      // at all (user, 2026-09-16: boss 5 reported done at over half health).
      seen: monstersNear(latestWorld, Infinity).slice(0, 5),
      landed,
    };
  }

  function describeFight(fight) {
    return `${fight.cleared ? "hết quái" : "hết giờ"} sau ${fight.seconds.toFixed(1)}s, ${fight.rounds} vòng`
      + (fight.unseen ? `, game báo không đánh được ${fight.unseen} con (lý do ${fight.refusals})` : "")
      + (fight.untouched ? `, bỏ hẳn ${fight.untouched} con còn đầy máu` : "")
      + (fight.last ? ` [${fight.last.state}: ${fight.last.detail}]` : "")
      + (fight.landed
        ? `, đòn cuối trúng #${fight.landed.target} -${fight.landed.damage} `
          + `(${((Date.now() - fight.landed.at) / 1000).toFixed(1)}s trước)`
        : ", không đánh trúng gì")
      + ` {quanh ta: ${fight.seen?.length
        ? fight.seen.map((monster) => `${monster.name || `#${monster.id}`} ${Math.round(monster.d)}px `
          + `máu ${Math.round(monster.hp / 2)}% tt ${monster.state}`).join(", ")
        : "không thấy con nào"}}`;
  }

  // Ends a walk on the game's own signals: a map change for a portal, or our
  // client reporting it stopped (MOVE_CLIENT moving bit cleared). A monster
  // attacking close by interrupts it. Without decoded traffic it just waits.
  // idleSeconds (coordinate walks): give up early when the click did not start
  // a walk at all, e.g. it picked a spot the pathfinder refuses.
  // settleSeconds (coordinate walks): how long we must stand still to count as
  // arrived; a long auto-path pauses for a moment on the way.
  async function waitForArrival(token, macro, step, since, { idleSeconds = 0, settleSeconds = 0 } = {}) {
    const deadline = since + Number(step.wait_seconds ?? macro.step_delay_seconds ?? 1) * 1000;
    const stillMs = Number(step.portal
      ? macro.portal_settle_seconds || 2.5
      : settleSeconds || macro.arrive_settle_seconds || 0.8) * 1000;
    while (Date.now() < deadline) {
      await domWait(token, 0.3);
      const world = latestWorld;
      if (!world?.frames) continue;
      if (mapSwitchedAt > since) {
        await domWait(token, macro.map_load_seconds || 1.5);
        return { how: "map", note: `qua cổng sang map ${latestWorld.mapId}` };
      }
      // Only with fight_on_the_way: pressing Đánh makes the character stop to
      // fight whatever is near instead of walking on (user, 2026-09-14).
      if (macro.combat_check !== false && macro.fight_on_the_way === true && !step.ignore_ambush) {
        const attackers = monstersNear(world, Number(macro.ambush_radius || 150), macro.ignore_monster_names)
          .filter((monster) => monster.state & 2);
        if (attackers.length) return { how: "ambush", note: `bị ${attackers.length} quái đánh khi đang đi` };
      }
      if (idleSeconds && !(world.meMovedAt > since) && Date.now() - since >= idleSeconds * 1000) {
        return { how: "idle", note: `không nhúc nhích sau ${idleSeconds}s` };
      }
      if (world.meMovedAt > since && !world.meMoving && Date.now() - world.meMovedAt >= stillMs) {
        const seconds = ((Date.now() - since) / 1000).toFixed(1);
        return step.portal
          ? { how: "stuck", note: `đứng ở cổng ${seconds}s mà chưa qua` }
          : { how: "arrived", note: `tới nơi sau ${seconds}s` };
      }
    }
    return {
      how: "timeout",
      note: latestWorld?.frames ? "hết giờ chờ đi" : "chờ theo giờ (chưa đọc được dữ liệu game)",
    };
  }

  // ignoredNames: units that fight on our side, e.g. a companion general that
  // follows us around, would otherwise read as a monster that never goes away.
  function monstersNear(world, radius, ignoredNames = []) {
    if (!world?.me) return [];
    const ignored = new Set(ignoredNames);
    return world.creatures
      .map(([id, x, y, hp, state, name, npc]) => ({
        id, x, y, hp, state, name, npc, d: Math.hypot(x - world.me.x, y - world.me.y),
      }))
      .filter((monster) => !monster.npc && !ignored.has(monster.name) && monster.d <= radius)
      .sort((a, b) => a.d - b.d);
  }

  function describeWorld() {
    const world = latestWorld;
    if (!world) return "chưa nhận dữ liệu game (bấm F5 tab game sau khi reload extension)";
    if (!world.frames) {
      return `có ${world.messages} gói nhưng không giải mã được (byte đầu: ${world.firstBytes || "?"})`;
    }
    const npcs = world.creatures.filter((creature) => creature[6]).length;
    return `map ${world.mapId ?? "?"}; ta ${world.me ? `${world.me.x},${world.me.y}` : "?"}; `
      + `${world.creatures.length - npcs} quái + ${npcs} NPC đang thấy; `
      + `${world.frames} gói, lỗi ${world.desync + world.badSegments}`;
  }

  // From the decoded game traffic (network_probe.js):
  //   "clear"   no live monster within monster_radius of us - move on;
  //   "combat"  at least one - keep fighting;
  //   "unknown" traffic not decoded - callers fall back to the time caps.
  function combatState(macro, skipped = new Set(), { idle = true } = {}) {
    const world = latestWorld;
    if (!world?.frames || !world.me) return { state: "unknown", detail: describeWorld() };
    const radius = Number(macro.monster_radius || 250);
    const around = monstersNear(world, radius, macro.ignore_monster_names);
    // Monsters in the fight: attacking us (STATE_ATTACK) or wounded, and idle
    // full-health ones close by - a boss waits idle until hit (Thiên Long
    // trận). Ones that never die (e.g. "Tượng đá cơ quan") go on the ignore list.
    // idle false (clear-before-door steps): only attackers and wounded ones -
    // none left is our yellow name showing again, the door opens (user,
    // 2026-09-14).
    const idleRadius = Number(macro.idle_monster_radius ?? 160);
    // One the walk grid puts behind walls - near in a straight line, a long way
    // round on foot (Hà Đông ngoài: two rows of walls) - is out of this fight:
    // the game cannot hit it from here. Leave it and move on (user, 2026-09-14).
    const unit = Number(macro.coord_unit || 8);
    const pathLimit = Number(macro.monster_path_tiles || 40);
    const grid = walkGridFor(world.mapId);
    const steps = grid && walkDistances(grid, { x: Math.floor(world.me.x / unit), y: Math.floor(world.me.y / unit) }, pathLimit);
    const onFoot = (monster) => {
      if (!steps) return true;
      const [tx, ty] = [Math.floor(monster.x / unit), Math.floor(monster.y / unit)];
      for (let dy = -1; dy <= 1; dy += 1) {
        for (let dx = -1; dx <= 1; dx += 1) {
          const [x, y] = [tx + dx, ty + dy];
          if (x >= 0 && y >= 0 && x < grid.w && y < grid.h && steps[y * grid.w + x] <= pathLimit) return true;
        }
      }
      return false;
    };
    const walled = around.filter((monster) => !onFoot(monster));
    const near = around.filter((monster) => onFoot(monster) && !skipped.has(monster.id) && ((monster.state & 2)
      || (monster.hp > 0 && monster.hp < 200) || (idle && monster.d <= idleRadius)));
    const standing = around.length - near.length - walled.length;
    const detail = `${near.length} quái đang đánh trong ${radius}px`
      + (walled.length ? ` (bỏ ${walled.length} con sau tường)` : "")
      + (standing > 0 ? ` (bỏ qua ${standing} con đứng yên)` : "")
      + (near.length
        ? ` (gần nhất ${near[0].name || `#${near[0].id}`} ${Math.round(near[0].d)}px, máu ${Math.round(near[0].hp / 2)}%)`
        : "");
    return { state: near.length ? "combat" : "clear", detail, near };
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
    const token = { cancelled: false, startedAt: Date.now() };
    domToken = token;
    updateDomFlow(flow, `Đang chạy không-CDP: ${flow}`);
    void (async () => {
      try {
        await startTimerKeepAlive();
        if (flow === "blessing") await runBlessing(token, macro);
        else if (flow === "code_redeem" || flow === "mch5exp_redeem") {
          await runCodeRedeem(token, macro, flow);
        }
        else if (flow === "discard_items") await runDiscardItems(token, macro);
        else if (flow === "use_inventory_item") await runUseInventoryItem(token, macro);
        else if (flow === "warehouse_take") await runWarehouseTake(token, macro);
        else if (flow === "coin_shake") await runCoinShake(token, macro);
        else if (flow === "auto_attack") await runAutoAttack(token, macro);
        else if (flow === "star_reappraisal") await runStarReappraisal(token, macro);
        else if (flow === "mount_skill_learn") await runMountSkillLearnOnce(token, macro);
        else if (macro.runner === "dungeon_route") await runDungeonRoute(token, macro, flow);
        else throw new Error(`Flow DOM chưa hỗ trợ: ${flow}`);
        domFlow = { state: "done", flow, message: "Flow hoàn tất" };
      } catch (error) {
        domFlow = token.cancelled || error.message === "FLOW_STOPPED"
          ? { state: "stopped", flow, message: "Đã dừng flow" }
          : { state: "error", flow, message: error.message };
      } finally {
        token.cancelled = true;
        stopTimerKeepAlive();
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
