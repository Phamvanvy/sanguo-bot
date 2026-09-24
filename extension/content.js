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
    "instance_reset_once",
    "dungeon_pipeline",
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

  // Chosen run counts per pipeline stage ("flowId:macro" -> n), kept in this
  // browser so the panel opens with the last numbers used.
  const STAGE_COUNTS_KEY = "sanguo-stage-counts";
  let stageCounts = {};
  try { stageCounts = JSON.parse(localStorage.getItem(STAGE_COUNTS_KEY) || "{}") || {}; } catch (_) { stageCounts = {}; }
  function saveStageCounts() {
    try { localStorage.setItem(STAGE_COUNTS_KEY, JSON.stringify(stageCounts)); } catch (_) { /* private window */ }
  }

  // A pipeline card: one count box per stage (0 skips it) and a run button, so
  // one card runs hard only, easy only, or n hard + m easy (user, 2026-09-23).
  function renderPipelineCard(flow, running) {
    const card = document.createElement("div");
    card.className = "sg-flow sg-item-flow";
    card.innerHTML = `<span></span><strong></strong><small></small><div class="sg-stage-counts"></div>`;
    card.querySelector("span").textContent = flow.icon;
    card.querySelector("strong").textContent = flow.label;
    card.querySelector("small").textContent = flow.description;
    const box = card.querySelector(".sg-stage-counts");
    const countOf = (stage) => {
      const saved = Number(stageCounts[`${flow.id}:${stage.macro}`]);
      return Number.isInteger(saved) && saved >= 0 ? saved : stage.times;
    };
    for (const stage of flow.stages) {
      const label = document.createElement("label");
      label.textContent = stage.label;
      const input = document.createElement("input");
      input.type = "number";
      input.min = "0";
      input.max = "50";
      input.step = "1";
      input.value = String(countOf(stage));
      input.disabled = running;
      input.addEventListener("change", () => {
        const times = Math.max(0, Math.min(50, Math.floor(Number(input.value) || 0)));
        input.value = String(times);
        stageCounts[`${flow.id}:${stage.macro}`] = times;
        saveStageCounts();
      });
      label.append(input);
      box.append(label);
    }
    const button = document.createElement("button");
    button.type = "button";
    button.disabled = running;
    button.textContent = "Chạy";
    button.addEventListener("click", () => {
      // Read the boxes as they are now, in case one was typed into without
      // leaving it (no change event yet).
      box.querySelectorAll("input").forEach((input) => input.dispatchEvent(new Event("change")));
      runFlow(flow, { stages: flow.stages.map((stage) => ({ macro: stage.macro, times: countOf(stage) })) });
    });
    box.append(button);
    return card;
  }

  // Redrawn only when something it shows changes: the status refresh runs
  // every 1.2 s and a redraw would throw away a count box being typed into.
  let renderedKey = "";
  function renderFlows(running) {
    const key = `${running}|${flows.map((flow) => flow.id).join(",")}`;
    if (key === renderedKey) return;
    renderedKey = key;
    flowsNode.replaceChildren(...flows.map((flow) => {
      if (flow.stages?.length) return renderPipelineCard(flow, running);
      // A flow that can run a chosen number of times gets a button per count
      // (the first is the default), e.g. the Thiên Long pipeline: 5 / 3 / 1.
      if (flow.run_options?.length) {
        const card = document.createElement("div");
        card.className = "sg-flow sg-item-flow";
        card.innerHTML = `<span></span><strong></strong><small></small><div class="sg-item-slots"></div>`;
        card.querySelector("span").textContent = flow.icon;
        card.querySelector("strong").textContent = flow.label;
        card.querySelector("small").textContent = flow.description;
        const slots = card.querySelector(".sg-item-slots");
        for (const times of flow.run_options) {
          const button = document.createElement("button");
          button.type = "button";
          button.disabled = running;
          button.textContent = `${times} lượt`;
          button.addEventListener("click", () => runFlow(flow, { times }));
          slots.append(button);
        }
        return card;
      }
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

  // The game socket's opens and closes since `from`, newest first.
  function socketEventsSince(from) {
    let events = [];
    try {
      events = JSON.parse(localStorage.getItem(NETWORK_QUEUE_KEY) || "[]");
      const lastEvent = JSON.parse(localStorage.getItem(NETWORK_EVENT_KEY) || "null");
      if (lastEvent) events.push(lastEvent);
    } catch (_) { /* Diagnostics must not break input when storage is unavailable. */ }
    return events
      .filter((event) => ["ws_open", "ws_close"].includes(event.type) && Number(event.at || 0) >= from)
      .sort((left, right) => Number(right.at || 0) - Number(left.at || 0));
  }

  function ensureDomActive(token) {
    if (token.cancelled) throw new Error("FLOW_STOPPED");
    const socketEvent = socketEventsSince(Number(token.socketCheckFrom || token.startedAt || 0))[0];
    // Opening Chọn NV makes the client close its own socket (code 1005) and
    // log in again on a new one a second later (2026-09-23 16:45), so while a
    // character switch runs a closed socket is expected, not a failure.
    if (socketEvent?.type === "ws_close" && !token.reconnecting) {
      const reason = socketEvent.reason ? ` (${socketEvent.reason})` : "";
      const error = new Error(`WebSocket bị đóng: code ${socketEvent.code}${reason}. Flow đã tự dừng.`);
      // A dungeon pipeline waits for the game to log in again and goes on.
      error.socketClosed = { code: Number(socketEvent.code), at: Number(socketEvent.at || Date.now()) };
      throw error;
    }
  }

  // The game dropped us mid-run (code 1006: the connection cut off, nothing
  // said - it has happened most days since 2026-08-12, flow or no flow). The
  // client logs in again by itself (21:08, 3 s) or once the user does (23:53,
  // 3 min). Wait for that: a new socket, then the client's own "map loaded".
  // User, 2026-09-23: a run cut off like that should recover, not stop.
  async function awaitReconnect(token, macro, flow, closed) {
    const limit = Number(macro.reconnect_wait_seconds ?? 300) * 1000;
    const deadline = Date.now() + limit;
    token.reconnecting = true;
    try {
      let openedAt = 0;
      while (Date.now() < deadline) {
        updateDomFlow(flow, `Mất kết nối (code ${closed.code}), chờ game vào lại`
          + ` (${Math.round((deadline - Date.now()) / 1000)}s)`, "reconnect");
        const latest = socketEventsSince(closed.at + 1)[0];
        openedAt = latest?.type === "ws_open" ? Number(latest.at) : 0;
        if (openedAt && (latestWorld?.mapLoadedAt || 0) > openedAt && latestWorld?.mapId != null) break;
        await domWait(token, 1);
      }
      if (!openedAt || !((latestWorld?.mapLoadedAt || 0) > openedAt)) {
        throw new Error(`Mất kết nối (code ${closed.code}) và game chưa vào lại sau ${limit / 1000}s. Flow đã tự dừng.`);
      }
      await domWait(token, Number(macro.reconnect_settle_seconds ?? 3));
      // Only a close after this new socket counts from here on.
      token.socketCheckFrom = openedAt;
    } finally {
      token.reconnecting = false;
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
    // Our own panel can sit over the game wherever the user dragged it: a
    // click meant for the game under it still goes to the game's canvas.
    const hit = document.elementFromPoint(x, y);
    const target = hit && panel.contains(hit) ? document.querySelector("#screen") || hit : hit;
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

  // Press, slide, release - the way a person scrolls the game's lists: their
  // arrow buttons do not scroll them when clicked from here (Map panel,
  // 2026-09-14; left menu, 2026-09-23). Every event goes to the element under
  // the start point, so our panel over the path does not get in the way.
  async function domDrag(token, from, to, { steps = 12, stepMs = 30 } = {}) {
    ensureDomActive(token);
    const start = eventTargetAt(from);
    const end = eventTargetAt(to);
    const { target } = start;
    if (typeof target.focus === "function") target.focus({ preventScroll: true });
    dispatchMouse(target, "mousedown", start.x, start.y, 1);
    for (let i = 1; i <= steps; i += 1) {
      await new Promise((resolve) => setTimeout(resolve, stepMs));
      const x = start.x + ((end.x - start.x) * i) / steps;
      const y = start.y + ((end.y - start.y) * i) / steps;
      dispatchMouse(target, "mousemove", x, y, 1);
    }
    // Held still a moment first, so it ends as a drag and not a fling or a tap.
    await new Promise((resolve) => setTimeout(resolve, 120));
    dispatchMouse(target, "mouseup", end.x, end.y, 0);
    return { target, x: start.x, y: start.y, toX: end.x, toY: end.y };
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

  // "Vứt bỏ" as the game draws it: its dark letters (red < 135) on the
  // yellow button, cut from the user's screenshot (2026-09-24) in a 1918x959
  // game canvas. One hex string per pixel row, 94 x 22.
  const VUT_BO_TEXT = {
    width: 94,
    rows: [
      "f00078078003000380000fc0", "f0007807800f0003800001e0", "f800f00e000f0003800001e0",
      "7800f004000f0003800003c0", "7c01e000000f000380000000", "3c01e000001f0003c0000000",
      "3c03c1e0ffbfe003ff801fe0", "3e03c1e0ff3fe003ffe0fff0", "1e03c1e0f31f0003f3e0fdf8",
      "1e0381e0f30f0003e1f1f0f8", "1f0701e0f30f0003c0f1f03c", "0f0f01e0fe0f0003c0f3e03c",
      "078f01e0fc0f000380f3c03c", "078e01e0f00f000380f3c03c", "079e01e0f00f000380f3c03c",
      "039e01e0f00f000380f3c03c", "03fc01e0f00f0003c0f3e07c", "03fc01e1f00f0003c1e1f0f8",
      "01f801f3f00f0003e1e0f9f8", "00f801fff00fe003ffe0fff8", "00f800fff007e003ff803fe0",
      "00f0007e7003c0033f001fc0",
    ],
  };
  // The canvas size the text above was cut at; any other size is scaled to it.
  const TEXT_CANVAS = [1918, 959];

  // The game canvas as pixels, scaled to TEXT_CANVAS. Read straight off the
  // canvas; if the page will not give its pixels (blank or tainted), from a
  // screenshot of the tab instead.
  async function readGameScreen() {
    const [width, height] = TEXT_CANVAS;
    const board = document.createElement("canvas");
    board.width = width;
    board.height = height;
    const context = board.getContext("2d", { willReadFrequently: true });
    const screen = document.querySelector("#screen");
    if (!screen) throw new Error("Không thấy canvas #screen của game");
    try {
      context.drawImage(screen, 0, 0, width, height);
      const image = context.getImageData(0, 0, width, height);
      let lit = 0;
      for (let i = 0; i < image.data.length; i += 4 * 97) if (image.data[i] || image.data[i + 1] || image.data[i + 2]) lit += 1;
      if (lit > 100) return { image, source: "canvas" };
    } catch (_) { /* tainted: fall through to a screenshot */ }
    const { dataUrl } = await send({ type: "capture" });
    const shot = await createImageBitmap(await (await fetch(dataUrl)).blob());
    const rect = screen.getBoundingClientRect();
    const scale = shot.width / innerWidth;
    context.drawImage(shot, rect.left * scale, rect.top * scale, rect.width * scale, rect.height * scale, 0, 0, width, height);
    return { image: context.getImageData(0, 0, width, height), source: "ảnh chụp tab" };
  }

  // Where a text's dark letters best overlap the dark pixels on screen, inside
  // area ([x0, y0, x1, y1], canvas fractions). Score = overlap / union of the
  // two masks, 1 for a perfect match; the popup's plain brown panel scores ~0.2.
  function findText(image, text, area) {
    const { width, height, data } = image;
    const dark = new Uint8Array(width * height);
    for (let i = 0; i < dark.length; i += 1) dark[i] = data[i * 4] < 135 ? 1 : 0;
    // Summed dark counts, so a window's total costs four lookups.
    const sums = new Int32Array((width + 1) * (height + 1));
    for (let y = 0; y < height; y += 1) {
      let row = 0;
      for (let x = 0; x < width; x += 1) {
        row += dark[y * width + x];
        sums[(y + 1) * (width + 1) + x + 1] = sums[y * (width + 1) + x + 1] + row;
      }
    }
    const letters = [];
    text.rows.forEach((hex, y) => {
      for (let x = 0; x < text.width; x += 1) {
        if ((parseInt(hex[x >> 2], 16) >> (3 - (x & 3))) & 1) letters.push(y * width + x);
      }
    });
    const textH = text.rows.length;
    const inWindow = (x, y) => sums[(y + textH) * (width + 1) + x + text.width] - sums[y * (width + 1) + x + text.width]
      - sums[(y + textH) * (width + 1) + x] + sums[y * (width + 1) + x];
    const scoreAt = (x, y) => {
      const around = inWindow(x, y);
      if (around < letters.length / 2) return 0;       // cannot reach 0.5
      const base = y * width + x;
      let both = 0;
      for (const at of letters) both += dark[base + at];
      return both / (letters.length + around - both);
    };
    const [x0, y0] = [Math.round(area[0] * width), Math.round(area[1] * height)];
    const [x1, y1] = [Math.round(area[2] * width) - text.width, Math.round(area[3] * height) - textH];
    let best = { x: 0, y: 0, score: 0 };
    for (let y = y0; y <= y1; y += 2) {
      for (let x = x0; x <= x1; x += 2) {
        const score = scoreAt(x, y);
        if (score > best.score) best = { x, y, score };
      }
    }
    // Coarse steps of 2, then every pixel around the best one.
    const coarse = best;
    for (let y = coarse.y - 2; y <= coarse.y + 2; y += 1) {
      for (let x = coarse.x - 2; x <= coarse.x + 2; x += 1) {
        if (x < 0 || y < 0 || x > width - text.width || y > height - textH) continue;
        const score = scoreAt(x, y);
        if (score > best.score) best = { x, y, score };
      }
    }
    return { point: [(best.x + text.width / 2) / width, (best.y + textH / 2) / height], score: best.score };
  }

  // Sort, open the first item, "Vứt bỏ", confirm - as before, except that
  // "Vứt bỏ" is found by its text: it moves with the item's kind (the 4th
  // button for equipment, under Mặc / Nâng cấp / Siêu cấp sao - user,
  // 2026-09-24), so a fixed point hit the wrong button.
  async function runDiscardItems(token, macro) {
    const maxCycles = Number(macro.max_cycles || 0);
    const area = macro.discard_search_area || [0.64, 0.15, 0.82, 0.92];
    const minScore = Number(macro.discard_min_score ?? 0.65);
    for (let cycle = 0; maxCycles <= 0 || cycle < maxCycles; cycle += 1) {
      await domClick(token, macro.sort_point || [0.796, 0.919]);
      await domWait(token, Number(macro.sort_delay_seconds || 0.9));
      await domClick(token, macro.first_item_point || [0.342, 0.229]);
      await domWait(token, Number(macro.detail_delay_seconds || 0.7));
      const { image, source } = await readGameScreen();
      const found = findText(image, VUT_BO_TEXT, area);
      appendDiagnostic("discard_find", {
        flow: "discard_items",
        message: `Vứt bỏ: ${source}, điểm ${found.score.toFixed(2)} tại ${found.point.map((v) => v.toFixed(3)).join(",")}`,
      });
      if (found.score < minScore) {
        throw new Error(`Không thấy nút "Vứt bỏ" (giống nhất ${Math.round(found.score * 100)}%) - popup món đồ chưa mở?`);
      }
      await domClick(token, found.point);
      await domWait(token, Number(macro.confirm_delay_seconds || 0.7));
      await domClick(token, macro.confirm_point || [0.685, 0.631]);
      updateDomFlow("discard_items", `Đã vứt: ${cycle + 1} vật phẩm`);
      await domWait(token, Number(macro.refresh_delay_seconds || 1));
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

  // Only the skill buttons: the Đánh button is left alone unless a macro still
  // gives an attack_point (user, 2026-09-21 - no more pressing Đánh).
  async function attackRound(token, macro) {
    const skillPoints = macro.skill_points || [
      [0.927, 0.517], [0.853, 0.566], [0.799, 0.670], [0.875, 0.710],
      [0.927, 0.653], [0.774, 0.820], [0.845, 0.820],
    ];
    if (macro.attack_point) {
      await domClick(token, macro.attack_point);
      await domDelay(macro.button_delay_seconds || 0.18);
    }
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
  function describeClick({ target, x, y, toX, toY }) {
    const name = `${target.tagName?.toLowerCase() || "?"}${target.id ? `#${target.id}` : ""}`;
    const drag = toX == null ? "" : `→${Math.round(toX)},${Math.round(toY)}`;
    return `${name}@${Math.round(x)},${Math.round(y)}${drag}`;
  }

  // Every step is logged to logs/extension-network.log (types dungeon_*),
  // so a route that drifts can be traced to the step and click that missed.
  async function runDungeonRoute(token, macro, flow) {
    // Started inside the dungeon (e.g. after a stop): the entry is behind us.
    const entryMap = macro.entry_map == null ? null : Number(macro.entry_map);
    const inside = entryMap != null && latestWorld?.mapId != null && latestWorld.mapId !== entryMap;
    const planned = [...(inside ? [] : macro.entry_steps || []), ...(macro.route_steps || [])];
    // A step may say which map(s) it runs on (maps: [...]). Then the map we
    // stand on picks where to start (user, 2026-09-23): already in Hà Đông's
    // quan nha means straight to its fight, whatever came before is behind us.
    const here = latestWorld?.mapId;
    const resumeAt = here == null ? -1
      : planned.findIndex((step) => (step.maps || []).map(Number).includes(here));
    const steps = resumeAt > 0 ? planned.slice(resumeAt) : planned;
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
        + (inside ? "; đã ở trong phó bản nên bỏ qua bước vào cửa" : "")
        + (resumeAt > 0 ? `; đang ở map ${here} nên bắt đầu từ "${steps[0].label}"` : ""),
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
      // Thiên Long's exit stays shut after the last boss: the game keeps saying
      // monsters are attacking until the character is switched and back (user,
      // 2026-09-23), so that happens right before walking out.
      if (step.switch_actor_before) {
        updateDomFlow(flow, `${title} (đổi nhân vật cho hết kẹt)`, `step_${index + 1}`);
        const mapBefore = latestWorld?.mapId;
        await switchActorAndBack(token, macro, flow);
        // Logging back in reloads the map; that is not this portal being
        // crossed - unless it put us somewhere else.
        if (latestWorld?.mapId === mapBefore) mapMark = mapSwitchedAt;
        notes.push(`đổi nhân vật xong, đang ở map ${latestWorld?.mapId ?? "?"}`);
      }
      const crossed = Boolean(step.portal) && mapSwitchedAt > mapMark;
      if (crossed) notes.push(`đã sang map ${latestWorld.mapId ?? "?"} từ trước nên bỏ qua`);
      let relogged = false;
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
        const doorFight = await fightUntilClear(token, macro, macro.ambush_fight_seconds || 60, {
          minRounds: relogged ? Number(step.extra_rounds_after_relog ?? macro.extra_rounds_after_relog ?? 6) : 0,
        });
        notes.push(describeFight(doorFight));
        appendDiagnostic("dungeon_ambush", { flow, message: `${title}: ${notes.slice(-2).join(" → ")}` });
        // Cleared and the door still shut: the game keeps the "being attacked"
        // mark with nothing left alive, and only logging in again drops it -
        // the same character will do (user, 2026-09-23, Hà Đông khó 22,8).
        // Nothing there to fight at all = cleared already and still marked:
        // log in again at once (user, 2026-09-24).
        if (shut && step.switch_actor_if_stuck && !relogged && (attempt >= 1 || doorFight.rounds === 0)) {
          relogged = true;
          updateDomFlow(flow, `${title} (hết quái mà cửa vẫn khóa: vào lại nhân vật)`, `step_${index + 1}`);
          const mapBefore = latestWorld?.mapId;
          await switchActorAndBack(token, macro, flow);
          if (latestWorld?.mapId === mapBefore) mapMark = mapSwitchedAt;
          notes.push(`vào lại nhân vật, đang ở map ${latestWorld?.mapId ?? "?"}`);
        }
        if (attempt >= Number(macro.max_rewalks ?? 3)) {
          if (shut) throw new Error(`${title}: vẫn chưa qua được cổng sau ${attempt + 1} lần`);
          break;
        }
      }
      if (step.touch_npc) notes.push(await touchNpcByName(token, step));
      if (step.touch_npc_by_map) notes.push(await touchNpcByMap(token, macro, step, clicks));
      if (step.answer_question != null) notes.push(await answerQuestion(token, macro, step));
      if (step.portal) mapMark = Math.max(mapMark, mapSwitchedAt);
      if (step.fight_seconds) {
        const until = step.clear_room
          ? "sạch phòng (xe, tượng chết thì boss mới ra)"
          : (step.ignore_idle_monsters ? "không còn quái đánh mình" : "hết quái");
        updateDomFlow(flow, `${title} (đánh tới khi ${until})`, `step_${index + 1}`);
        // One step can serve two versions of a dungeon (Cổ Mộ dễ and khó share
        // this route), and the hard one's last boss simply has more health, so
        // its cap is per map id (user, 2026-09-19).
        const seconds = (step.fight_seconds_by_map || {})[latestWorld?.mapId] ?? step.fight_seconds;
        notes.push(describeFight(await fightUntilClear(token, macro, seconds, {
          idle: !step.ignore_idle_monsters,
          radius: step.monster_radius,
          idleRadius: step.idle_monster_radius,
          ignoreNames: step.ignore_monster_names,
          clearRoom: Boolean(step.clear_room),
          // click_boss: the boss stands on this step's goto tile, unseen by the
          // unit list - click it there and fight a few rounds at least.
          bossAt: step.click_boss && step.goto
            ? { x: (Number(step.goto[0]) + 0.5) * Number(macro.coord_unit || 8),
              y: (Number(step.goto[1]) + 0.5) * Number(macro.coord_unit || 8) - Number(step.boss_click_lift ?? 20) }
            : null,
          minRounds: Number(step.min_rounds ?? (step.click_boss ? 12 : 0)),
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

  // "Reset p.bản" in the game's Đội panel, sent as the packet the button itself
  // sends instead of clicking through H.Trang -> Đội -> Reset p.bản (user,
  // 2026-09-21: a command beats the UI). OpCode.INSTANCE_CLEAR_CLIENT = 534,
  // body just an int serial; the server answers 535 when the progress is
  // cleared, or ERROR (-1) carrying its own refusal text. See
  // Game/sangobuildVn/server/src/peony/game/{OpCode,PlayerPacketHandler}.java.
  const OP_INSTANCE_CLEAR = 534;
  // Well above anything the game's own client counts up to, so our answer is
  // never mistaken for one of its pending requests.
  let commandSerial = 900000;

  // Hands one packet to network_probe.js and resolves once it has gone out (or
  // with the reason it could not). Nothing waits for a game answer here.
  async function postGamePacket(opcode, fields) {
    const id = `${Date.now()}-${(commandSerial += 1)}`;
    const sent = new Promise((resolve) => {
      const onMessage = (event) => {
        if (event.source !== window || event.data?.source !== "sanguo-sent") return;
        if (event.data.id !== id) return;
        window.removeEventListener("message", onMessage);
        resolve(String(event.data.error || ""));
      };
      window.addEventListener("message", onMessage);
      setTimeout(() => {
        window.removeEventListener("message", onMessage);
        resolve("network_probe.js không trả lời (bấm F5 tab game sau khi reload extension)");
      }, 2000);
    });
    window.postMessage({ source: "sanguo-send", id, opcode, fields }, location.origin);
    return sent;
  }

  async function sendGameCommand(token, macro, opcode) {
    const serial = (commandSerial += 1);
    const error = await postGamePacket(opcode, [["i32", serial]]);
    if (error) throw new Error(error);
    const deadline = Date.now() + Number(macro.reply_seconds || 8) * 1000;
    while (Date.now() < deadline) {
      const reply = (latestWorld?.replies || []).find((item) => item.serial === serial);
      if (reply) return reply;
      await domWait(token, 0.2);
    }
    return null;
  }

  // "Bấm vào NPC": OpCode.TOUCHNPC_CLIENT (120), int npc instanceId | int
  // questId. The NPC is found by name in the game's own unit list, so there is
  // no guessing where it sits on screen. The server refuses past 80 px ("Cự li
  // quá xa"). questId -1 is what the real client sends for a dungeon-entry NPC
  // (log 2026-09-23, Thái Trường Trị by hand): Player.touchNpc files it in
  // touchedNpc, where an area quest's E_TouchNPC looks (ASMGameVM.e_TouchNPC:
  // hasTouchNpc(npcId, -1)). questId <= -2 instead runs the NPC's touch action
  // list and never reaches the quest, so no popup ever came.
  const OP_TOUCH_NPC = 120;
  // OpCode.TOUCHEXIT_CLIENT: the client stepping into a door.
  const OP_TOUCH_EXIT = 116;
  let touchedAt = 0;                   // the answer step only takes a popup newer than this
  // withinPx: the server's reach for a TOUCHNPC packet. A click on the Map
  // needs none - the client walks up to the NPC first (Hà Đông 448, 21:13).
  function nearestNpc(name, withinPx = 80) {
    const world = latestWorld;
    if (!world?.me) throw new Error("Chưa đọc được dữ liệu game để tìm NPC");
    const near = world.creatures
      .map(([id, x, y, hp, state, unitName]) => ({ id, x, y, name: unitName }))
      .filter((unit) => unit.name === name)
      .map((unit) => ({ ...unit, d: Math.hypot(unit.x - world.me.x, unit.y - world.me.y) }))
      .sort((a, b) => a.d - b.d)[0];
    if (!near) {
      throw new Error(`Không thấy NPC "${name}" trong dữ liệu game (đang ở map ${world.mapId ?? "?"})`);
    }
    if (near.d >= withinPx) {
      throw new Error(`NPC "${name}" cách ${Math.round(near.d)}px, game chỉ cho bấm trong ${withinPx}px`);
    }
    return near;
  }

  async function touchNpcByName(token, step) {
    const near = nearestNpc(step.touch_npc);
    const questId = Number(step.touch_quest_id ?? -1);
    const since = Date.now();
    touchedAt = since;
    const error = await postGamePacket(OP_TOUCH_NPC, [["i32", near.id], ["i32", questId]]);
    if (error) throw new Error(error);
    const said = `bấm NPC ${step.touch_npc} #${near.id} cách ${Math.round(near.d)}px (questId ${questId})`;
    // The NPC's answer comes back as its own packet when the server's quest
    // asks. The client's own quest VM can also draw the popup without one, so
    // a silent server is not an error here: the answer step checks the map.
    const deadline = Date.now() + Number(step.dialog_seconds ?? 3) * 1000;
    while (Date.now() < deadline) {
      const popup = (latestWorld?.dialogs || []).find((dialog) => dialog.at >= since);
      if (popup) {
        return `${said}; game ${popup.kind} quest ${popup.questId} "${popup.message}"`
          + (popup.options ? ` [${popup.options}]` : "") + ` notifyId ${popup.notifyId}`;
      }
      await domWait(token, 0.2);
    }
    return `${said}; server không gửi thoại nào`;
  }
  // Answers the NPC's popup with the packet the client sends when the option
  // is clicked - no clicking where the buttons are hoped to be, and no
  // dependence on which unit the client has selected. NOTIFY_CLIENT (174):
  // int questId | byte notifyId | byte type (3 = question) | byte answer.
  // questId / notifyId come from the server's QUESTION packet when it sent
  // one, else from the step (copied from a real click). The answer is the
  // step's number as is: the live client sends 16 for "1. Vào Thiên Long Trận
  // chế độ Khó" (log 2026-09-23), not the 0 the original data would suggest.
  const OP_NOTIFY_CLIENT = 174;
  const NOTIFY_QUESTION = 3;
  async function answerQuestion(token, macro, step) {
    const asked = [...(latestWorld?.dialogs || [])].reverse()
      .find((dialog) => dialog.kind === "question" && dialog.at >= touchedAt);
    const questId = Number(asked?.questId ?? step.answer_quest_id);
    const notifyId = Number(asked?.notifyId ?? step.answer_notify_id);
    if (!Number.isFinite(questId) || !Number.isFinite(notifyId)) {
      throw new Error("Chưa thấy game hỏi gì, mà bước cũng không ghi answer_quest_id / answer_notify_id");
    }
    const answer = Number(step.answer_question);
    const since = Date.now();
    const error = await postGamePacket(OP_NOTIFY_CLIENT, [
      ["i32", questId], ["u8", notifyId], ["u8", NOTIFY_QUESTION], ["u8", answer],
    ]);
    if (error) throw new Error(error);
    const said = (asked ? `trả lời "${asked.message}" [${asked.options}]` : "trả lời (server không gửi câu hỏi)")
      + ` chọn ${answer} (questId ${questId}, notifyId ${notifyId})`;
    if (!step.portal) return said;
    // The answer is only right if it takes us somewhere: wait for the map.
    const deadline = since + Number(step.wait_seconds ?? 15) * 1000;
    while (Date.now() < deadline) {
      if (mapSwitchedAt > since) {
        await domWait(token, macro.map_load_seconds || 1.5);
        return `${said}; sang map ${latestWorld?.mapId ?? "?"}`;
      }
      await domWait(token, 0.3);
    }
    throw new Error(`${said}; nhưng vẫn ở map ${latestWorld?.mapId ?? "?"} sau ${step.wait_seconds ?? 15}s`);
  }

  // Touches an NPC the way a person does when the packet alone is not enough
  // (Thiên Long khó, 2026-09-23: the same TOUCHNPC + NOTIFY the client sends
  // did not take us in, and the popup is drawn by the client's own quest VM).
  // Opens Map, clicks the NPC on it (user: that opens the popup too), clicks
  // the popup's option, closes Map. The client's own packets prove each click:
  // a TOUCHNPC for that NPC after the Map click, a NOTIFY after the option.
  async function touchNpcByMap(token, macro, step, clicks) {
    // Not every NPC comes down in the unit list: Hà Đông's "Chiêu thảo sứ" on
    // map 448 never did (2026-09-23 21:13, "0 NPC đang thấy"), yet a Map click
    // on its tile made the client walk up and touch it. So with npc_tile set,
    // an NPC missing from the list is clicked there, and any touch the client
    // sends after that click counts; it walks the 80 px itself.
    let near = null;
    try {
      near = nearestNpc(step.touch_npc_by_map, Infinity);
    } catch (error) {
      if (!step.npc_tile || !/Không thấy NPC/.test(error.message)) throw error;
    }
    const me = latestWorld.me;
    const panel = mapPanel(macro, step.map_size, me);
    const unit = Number(macro.coord_unit || 8);
    // The sprite stands on its point; its body is drawn above it.
    const aim = near
      ? { x: near.x, y: near.y - Number(step.npc_click_lift ?? 20) }
      : { x: (Number(step.npc_tile[0]) + 0.5) * unit, y: (Number(step.npc_tile[1]) + 0.5) * unit };
    if (!panel.shows(aim)) {
      throw new Error(`NPC "${step.touch_npc_by_map}" ở ${Math.round(aim.x)},${Math.round(aim.y)} không nằm trong Map lúc mở`);
    }
    const said = near
      ? `bấm NPC ${near.name} #${near.id} trên Map`
      : `bấm ô ${step.npc_tile.join(",")} của NPC ${step.touch_npc_by_map} trên Map (không có trong dữ liệu game)`;
    const openedAt = await openMap(token, macro, clicks);
    await domWait(token, macro.map_open_delay_seconds || 1.2);
    const touchedSince = Date.now();
    clicks.push(await domClick(token, panel.toFraction(aim)));
    let touch = null;
    for (let deadline = Date.now() + Number(step.dialog_seconds ?? 4) * 1000; !touch && Date.now() < deadline;) {
      await domWait(token, 0.2);
      touch = (latestWorld?.touches || []).find((item) => item.at >= touchedSince && (!near || item.target === near.id));
    }
    if (!touch) {
      await closeMap(token, panel.closePoint, openedAt, clicks);
      throw new Error(`${said}: client không gửi lệnh bấm NPC nào - bấm trượt NPC trên Map`);
    }
    // Let the popup draw before clicking its option.
    await domWait(token, step.popup_delay_seconds ?? 1);
    const answeredSince = Date.now();
    clicks.push(await domClick(token, step.option_point));
    let answer = null;
    for (let deadline = Date.now() + 3000; !answer && Date.now() < deadline;) {
      await domWait(token, 0.2);
      answer = (latestWorld?.answers || []).find((item) => item.at >= answeredSince);
    }
    // The option closes the popup but not the Map: close it before the new map
    // loads, while its X is still where the lobby's Map put it - unless the new
    // map already came, which closed it.
    await closeMap(token, panel.closePoint, openedAt, clicks);
    const note = `${said} (client bấm NPC #${touch.target}, questId ${touch.questId}); `
      + (answer ? `chọn mục: client gửi quest ${answer.questId} notifyId ${answer.notifyId} answer ${answer.answer}`
        : "bấm mục trong popup nhưng client không gửi trả lời nào");
    if (!answer) throw new Error(note);
    if (!step.portal) return note;
    for (let deadline = answeredSince + Number(step.wait_seconds ?? 15) * 1000; Date.now() < deadline;) {
      if (mapSwitchedAt > answeredSince) {
        await domWait(token, macro.map_load_seconds || 1.5);
        return `${note}; sang map ${latestWorld?.mapId ?? "?"}`;
      }
      // The game says no in its own words, e.g. "Bản đồ phụ này mỗi ngày chỉ
      // có thể đi 5 lần" (Thiên Long khó, 2026-09-23 16:47).
      const refusal = (latestWorld?.dialogs || [])
        .find((dialog) => dialog.kind === "message" && dialog.at >= answeredSince);
      if (refusal) {
        const error = new Error(`${note}; game báo: ${refusal.message}`);
        error.dailyLimit = /mỗi ngày/i.test(refusal.message);
        throw error;
      }
      await domWait(token, 0.3);
    }
    throw new Error(`${note}; nhưng vẫn ở map ${latestWorld?.mapId ?? "?"} sau ${step.wait_seconds ?? 15}s`);
  }

  async function runInstanceReset(token, macro, flow) {
    updateDomFlow(flow, "Đang gửi lệnh xóa tiến độ phó bản", "send");
    appendDiagnostic("instance_reset_send", {
      flow, message: `gửi OpCode ${OP_INSTANCE_CLEAR}; ${describeWorld()}`,
    });
    const reply = await sendGameCommand(token, macro, OP_INSTANCE_CLEAR);
    if (!reply) throw new Error("Game không trả lời lệnh reset phó bản");
    // The game refuses while we are still standing inside an instance
    // (PlayerPacketHandler.instanceClear: player.getVMap().instance == null).
    if (!reply.ok) throw new Error(`Game từ chối: ${reply.message}`);
    // The client answers 535 with its own message box, "副本进度已清除!", up for
    // 3000 ms (ui_gamemenu.gtl, CONN_INSTANCE_CLEAR_SERVER). The next run's
    // first Map click landed on it, the Map never opened and the X opened Cầu
    // phúc (Cổ Mộ 2026-09-24 01:39, Hà Đông 21:57 and 20:17): let it go first.
    await domWait(token, Number(macro.reset_message_seconds ?? 4));
    updateDomFlow(flow, "Đã xóa tiến độ phó bản", "done");
    appendDiagnostic("instance_reset_done", { flow, message: `game trả lời OK; ${describeWorld()}` });
  }

  // Walks the game's own menu to Chọn NV and logs into a character, exactly the
  // path the user clicks by hand: H.Trang -> (cuộn menu trái) H.Thống -> Đổi
  // nhân vật -> ô nhân vật. Done by clicking, not by an ACTOR_LOGIN packet: the
  // point is to make the CLIENT rebuild its state, and a packet behind its back
  // would leave the page showing the old character.
  async function openActorPicker(token, switcher, clicks) {
    const need = (point) => {
      if (!point) throw new Error("Thiếu tọa độ trong actor_switch (xem config.yaml)");
      return point;
    };
    clicks.push(await domClick(token, need(switcher.hanh_trang_point)));
    await domWait(token, Number(switcher.panel_seconds ?? 1.5));
    // H.Thống only shows once the left menu is dragged to its end; its ▼ arrow
    // does nothing when clicked from here (user, 2026-09-23). Dragging past
    // the end leaves it at the end, so a spare drag is harmless.
    for (let n = 0; n < Math.max(1, Number(switcher.menu_drag_times ?? 2)); n += 1) {
      clicks.push(await domDrag(token, need(switcher.menu_drag_from), need(switcher.menu_drag_to)));
      await domWait(token, Number(switcher.scroll_seconds ?? 0.8));
    }
    clicks.push(await domClick(token, need(switcher.he_thong_point)));
    await domWait(token, Number(switcher.panel_seconds ?? 1.5));
    clicks.push(await domClick(token, need(switcher.doi_nhan_vat_point)));
    await domWait(token, Number(switcher.picker_seconds ?? 3));
  }

  // Which card in Chọn NV is which: the game says so itself. Opening the screen
  // makes the client ask for the character list (ACTOR_LIST_SERVER), which
  // arrives in the same order the cards are drawn, and the login packet says
  // which of them we are (network_probe.js). So the slots are read, not
  // configured; actor_slot / spare_actor_slot in config.yaml only override it.
  // listedBefore: only a list that arrived after our clicks proves Chọn NV is
  // really open (an older one says nothing about what is on screen now).
  async function readActorSlots(token, switcher, listedBefore = 0) {
    const deadline = Date.now() + Number(switcher.list_seconds ?? 6) * 1000;
    const fresh = () => (latestWorld?.actorsAt || 0) > listedBefore && (latestWorld?.actors || []).length;
    while (Date.now() < deadline && !fresh()) await domWait(token, 0.2);
    if (!fresh()) {
      throw new Error("Màn Chọn NV không mở (game không gửi danh sách nhân vật) - menu trái chưa bấm trúng H.Thống / Đổi nhân vật");
    }
    const actors = latestWorld?.actors || [];
    const forced = Number(switcher.actor_slot || 0);
    const spareForced = Number(switcher.spare_actor_slot || 0);
    let mine = forced > 0 ? forced - 1 : actors.findIndex((actor) => actor.id === latestWorld?.actorId);
    if (!(mine >= 0)) {
      throw new Error(actors.length
        ? "Không biết đang chơi nhân vật nào (bấm F5 tab game để probe đọc lúc đăng nhập),"
          + " hoặc đặt actor_slot trong config.yaml"
        : "Game chưa gửi danh sách nhân vật; đặt actor_slot và spare_actor_slot trong config.yaml");
    }
    const count = Math.max(actors.length, mine + 1);
    const spare = spareForced > 0 ? spareForced - 1 : [...Array(count).keys()].find((slot) => slot !== mine);
    const naming = (slot) => `ô ${slot + 1}${actors[slot] ? ` (${actors[slot].name} cấp ${actors[slot].level})` : ""}`;
    return { mine, spare: spare >= 0 && spare !== mine ? spare : -1, naming };
  }

  async function pickActorSlot(token, switcher, slot, clicks) {
    const point = (switcher.actor_slot_points || [])[slot];
    if (!point) throw new Error(`Thiếu actor_slot_points cho ô ${slot + 1}`);
    clicks.push(await domClick(token, point));
    await domWait(token, Number(switcher.login_seconds ?? 12));
  }

  // The trận keeps saying monsters are attacking after a run, and only a
  // character switch lets go of it - the user does this by hand between every
  // run (2026-09-23). Logging back into the same character does it.
  async function switchActorAndBack(token, macro, flow) {
    const switchedAt = Date.now();
    token.reconnecting = true;
    try {
      await switchActorAndBackOnce(token, macro, flow);
    } finally {
      token.reconnecting = false;
      // From here on only a socket that closes again, and stays closed,
      // stops the flow: the last word since the switch must be ws_open.
      token.socketCheckFrom = switchedAt;
    }
  }

  async function switchActorAndBackOnce(token, macro, flow) {
    const switcher = macro.actor_switch || {};
    const clicks = [];
    const mapBefore = latestWorld?.mapId ?? "?";
    const trail = () => `đã bấm: ${clicks.map(describeClick).join(" | ")}`;
    // A door refused while "in combat" reloads the map, and the client only
    // says so once it is done: H.Trang clicked meanwhile does nothing and Chọn
    // NV never opens (Hà Đông 450, 2026-09-24 10:18: clicked at :25.4, loaded
    // at :26.0). So give a reload a moment to show, then let the map settle.
    await domWait(token, Number(switcher.settle_seconds ?? 2));
    await waitMapReady(token, macro);
    // Opens Chọn NV and proves it by the character list the game sends.
    const openPicker = async () => {
      const listedBefore = latestWorld?.actorsAt || 0;
      await openActorPicker(token, switcher, clicks);
      try {
        return await readActorSlots(token, switcher, listedBefore);
      } catch (error) {
        throw new Error(`${error.message}; ${trail()}`);
      }
    };
    // Logs in on one card and proves it by the game's login packet.
    const loginAs = async (slot) => {
      const actor = (latestWorld?.actors || [])[slot];
      await pickActorSlot(token, switcher, slot, clicks);
      if (actor && latestWorld?.actorId !== actor.id) {
        throw new Error(`Bấm ô ${slot + 1} (${actor.name}) mà game chưa đăng nhập nhân vật đó; ${trail()}`);
      }
    };
    // Logging back into the SAME character is enough to drop the stuck state
    // (user, 2026-09-23): one login instead of two. via_spare: true keeps the
    // old way, away to another character first.
    const slots = await openPicker();
    let note = `vào lại ${slots.naming(slots.mine)}`;
    if (switcher.via_spare) {
      if (slots.spare < 0) throw new Error("Tài khoản chỉ có một nhân vật nên không đổi qua lại được");
      await loginAs(slots.spare);
      await openPicker();
      note = `đổi tạm sang ${slots.naming(slots.spare)} rồi ${note}`;
    }
    await loginAs(slots.mine);
    appendDiagnostic("actor_switch", {
      flow,
      message: `${note}; map trước ${mapBefore} sau ${latestWorld?.mapId ?? "?"}; `
        + `click: ${clicks.map(describeClick).join(" | ")}`,
    });
  }

  // Runs several dungeon macros back to back on the one character. Each run
  // switches characters itself before walking out (switch_actor_before), and
  // the dungeon's progress is cleared between runs so the next one starts clean
  // (user, 2026-09-23: đổi NV -> ra khỏi map -> reset phó bản -> chạy lại).
  async function runDungeonPipeline(token, macro, flow) {
    const plan = [];
    // times: how many of each stage. A stage can pin its own count; otherwise
    // the macro's default stands, and a panel button overrides that.
    for (const stage of macro.stages || []) {
      const times = Number(stage.times ?? macro.times ?? 1);
      for (let n = 0; n < times; n += 1) plan.push(String(stage.macro));
    }
    if (!plan.length) throw new Error("Chưa chọn lượt nào (mọi ô đều là 0)");
    appendDiagnostic("pipeline_start", { flow, message: `${plan.length} lượt: ${plan.join(", ")}` });
    const exhausted = new Set();        // stages the game says are done for today
    let resumes = 0;                    // runs picked up again after a dropped connection
    for (const [index, id] of plan.entries()) {
      const title = `Lượt ${index + 1}/${plan.length}`;
      if (exhausted.has(id)) continue;
      const config = await api(`/macro?id=${encodeURIComponent(id)}`);
      // Only from outside the instance: the game refuses inside one, and the
      // run before ended by walking out.
      let resetDone = index === 0;
      for (;;) {
        try {
          if (!resetDone) {
            await runInstanceReset(token, macro, flow);
            resetDone = true;
          }
          updateDomFlow(flow, `${title}: ${config.macro.label || id}`, `run_${index + 1}`);
          appendDiagnostic("pipeline_run", { flow, message: `${title}: ${id}; ${describeWorld()}` });
          // One bell at the end of the whole pipeline, not after every run.
          await runDungeonRoute(token, { ...config.macro, finish_chime: false }, flow);
          break;
        } catch (error) {
          // Cut off mid-run: wait for the game to log in again, then run this
          // one again - each step names its maps, so it picks up where we
          // stand (user, 2026-09-23). Only a plain drop, never a stop or guard.
          const closed = error.socketClosed;
          const codes = (macro.resume_close_codes || [1006]).map(Number);
          if (closed && codes.includes(closed.code) && resumes < Number(macro.max_resumes ?? 5)) {
            resumes += 1;
            appendDiagnostic("pipeline_resume", { flow, message: `${title}: ${error.message}; chờ vào lại (lần ${resumes})` });
            await awaitReconnect(token, macro, flow, closed);
            appendDiagnostic("pipeline_resume", { flow, message: `${title}: đã vào lại; ${describeWorld()}` });
            continue;
          }
          // Out of runs for today: skip the rest of this stage, go on with the
          // next one instead of stopping the whole pipeline.
          if (!error.dailyLimit) throw error;
          exhausted.add(id);
          appendDiagnostic("pipeline_skip", { flow, message: `${title}: bỏ các lượt ${id} còn lại - ${error.message}` });
          break;
        }
      }
    }
    updateDomFlow(flow, `Xong ${plan.length} lượt phó bản`, "done");
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

  // Clicks the Map button, but never while a new map is still loading: then
  // the button does nothing, the destination click lands on the main screen
  // and the Map's X - on a 512-wide map right over C.Phúc - opens Cầu phúc,
  // which swallows every click after it (Hà Đông pipeline, 2026-09-23: runs
  // that began the second the exit put us on map 448 stuck there for minutes).
  // Returns when it clicked, for closeMap.
  async function openMap(token, macro, clicks) {
    await waitMapReady(token, macro);
    const openedAt = Date.now();
    clicks.push(await domClick(token, macro.map_button_point || [0.85, 0.07]));
    return openedAt;
  }

  // Ready = the client said LOADING_FINISHED for the new map, and a moment
  // since: 4 s after the switch alone was not enough (Hà Đông 448, 21:57:
  // two Map clicks right after exit + reset did nothing, C.Phúc again).
  async function waitMapReady(token, macro) {
    if (mapSwitchedAt) {
      const deadline = Date.now() + Number(macro.map_load_wait_seconds ?? 20) * 1000;
      while (!((latestWorld?.mapLoadedAt || 0) >= mapSwitchedAt - 1000) && Date.now() < deadline) {
        await domWait(token, 0.3);
      }
    }
    const loadedAt = Math.max(mapSwitchedAt, latestWorld?.mapLoadedAt || 0);
    const readyAt = loadedAt + Number(macro.map_ready_seconds ?? 3) * 1000;
    if (Date.now() < readyAt) await domWait(token, (readyAt - Date.now()) / 1000);
  }

  // The Map's X - but not once the map has changed since it opened: going to
  // another map closes the Map by itself (user, 2026-09-23), and the X would
  // land on the top-bar button beneath it (C.Phúc / H.Trang).
  async function closeMap(token, point, openedAt, clicks) {
    if (mapSwitchedAt > openedAt) return "đổi map nên Map tự đóng, không bấm X";
    // The same map loaded again closes it too: a door stepped on and refused
    // shows the loading screen and reloads the map (Hà Đông 450, 2026-09-24
    // 01:15: walking to 25,9 crossed the exit tile 24,8, X then hit H.Động).
    if ((latestWorld?.mapLoadedAt || 0) > openedAt) return "map vừa tải lại nên Map tự đóng, không bấm X";
    clicks.push(await domClick(token, point));
    return "nút X";
  }

  // Opens Map, picks the destination and closes Map again: the panel never
  // closes by itself and would swallow every later click.
  async function walkByMap(token, macro, step, clicks) {
    const openedAt = await openMap(token, macro, clicks);
    await domWait(token, macro.map_open_delay_seconds || 1.2);
    clicks.push(await domClick(token, step.map_point));
    await domWait(token, macro.map_close_delay_seconds || 0.6);
    // The game's close key does not reach the Map from here: always its X button.
    return closeMap(token, step.map_close_point, openedAt, clicks);
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

  // Where a map pixel is drawn on the main screen, in canvas fractions. The
  // world is drawn at 2x (a map pixel is 2 logical screen pixels): fitted on
  // the user's screenshot, us at 81,57 and the portal circle at 85,59 drawn
  // 110,60 px apart on a 1906 px wide canvas. The camera follows us but stops
  // at the map's edges, and a map narrower (or shorter) than the screen shows
  // is drawn centred instead: Hà Đông's 448 (512 wide) and 432 (608 wide) in
  // the user's screenshots, where we stood 170 map px right of the middle.
  function screenPointOf(macro, point, me, mapSize) {
    const [screenW, screenH] = (macro.map_panel_screen || [1280, 640]).map(Number);
    const worldScale = Number(macro.world_scale ?? 2);
    const [mapW, mapH] = mapSize.map(Number);
    const along = (at, size, view) => (size <= view ? size / 2
      : Math.min(Math.max(at, view / 2), size - view / 2));
    const eye = { x: along(me.x, mapW, screenW / worldScale), y: along(me.y, mapH, screenH / worldScale) };
    return [
      0.5 + (point.x - eye.x) * worldScale / screenW,
      0.5 + (point.y - eye.y) * worldScale / screenH,
    ];
  }

  // Clear of the HUD: top bar, joystick bottom-left, skills bottom-right.
  function clearOfHud([x, y]) {
    return x > 0.08 && x < 0.92 && y > 0.15 && y < 0.85 && !(x > 0.7 && y > 0.5) && !(x < 0.22 && y > 0.6);
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
    let strayPopups = 0;
    let strayBlessings = 0;
    const summary = () => `đi tới ${step.goto.join(",")}: ${legs.join(" → ") || "đã đứng sẵn ở đó"}`;
    // Clicking the door on the Map can leave us standing still beside it
    // (Thiên Long's exit: stuck at 81,57 every time, and nudging on the Map
    // stayed too far off - user, 2026-09-23). On the main screen the camera
    // keeps us in the middle, so a tile near us can be clicked right where it
    // is drawn. Once the Map walk has failed, click the door there: the game
    // data's exits first (Thiên Long's portal circle sits on 85,59, the
    // user's 84,60 beside it), then the door, then the tiles around it.
    let nudged = false;
    const onScreen = (tile, me) => screenPointOf(macro, center(tile), me,
      walkGridFor(latestWorld?.mapId)?.size || step.map_size);
    const nudgeThrough = async ({ doorsOnly = false } = {}) => {
      if (!step.portal || nudged) return null;
      if (!doorsOnly) nudged = true;
      const grid = walkGridFor(latestWorld?.mapId);
      const goal = { x: cellX, y: cellY };
      const doors = doorTiles(grid, goal);
      const tiles = [...doors.slice(1), goal];
      const radius = doorsOnly ? 0 : Number(step.door_nudge_radius ?? macro.door_nudge_radius ?? 2);
      const seen = new Set(tiles.map((tile) => `${tile.x},${tile.y}`));
      for (let ring = 1; ring <= radius; ring += 1) {
        for (const door of [...doors.slice(1), goal]) {
          for (let dy = -ring; dy <= ring; dy += 1) {
            for (let dx = -ring; dx <= ring; dx += 1) {
              if (Math.max(Math.abs(dx), Math.abs(dy)) !== ring) continue;
              const tile = { x: door.x + dx, y: door.y + dy };
              if (!seen.has(`${tile.x},${tile.y}`)) {
                seen.add(`${tile.x},${tile.y}`);
                tiles.push(tile);
              }
            }
          }
        }
      }
      const nudgeSince = Date.now();
      for (const tile of tiles) {
        // Crossed late, after that tile's wait ran out: stop clicking this map's
        // tiles onto the next one.
        if (mapSwitchedAt > nudgeSince) {
          await domWait(token, macro.map_load_seconds || 1.5);
          return { how: "map", note: `qua cổng sang map ${latestWorld?.mapId}; ${summary()}` };
        }
        const me = await currentPosition(token);
        const point = onScreen(tile, me);
        if (!clearOfHud(point)) continue;
        const since = Date.now();
        clicks.push(await domClick(token, point));
        const walk = await waitForArrival(token, macro, {
          ...step, portal: true, wait_seconds: Number(macro.door_nudge_seconds ?? 6),
        }, since, { idleSeconds: 3 });
        legs.push(`bấm màn hình ${tile.x},${tile.y} ${walk.how} @${where(latestWorld?.me || me)}`);
        if (walk.how === "map" || walk.how === "ambush" || walk.refused) return { ...walk, note: `${walk.note}; ${summary()}` };
      }
      return null;
    };
    // A door already on screen is clicked right there, not through the Map: a
    // Map that did not open leaves its X click on whatever button sits below
    // it - C.Phúc on a 512-wide map - and Cầu phúc then swallows every click
    // after it (Hà Đông 448, 2026-09-23, run after run).
    if (step.portal) {
      const start = tileOf(await currentPosition(token));
      const away = Math.max(Math.abs(start.x - cellX), Math.abs(start.y - cellY));
      if (away <= Number(step.screen_door_tiles ?? macro.screen_door_tiles ?? 10)) {
        const through = await nudgeThrough({ doorsOnly: true });
        if (through) return through;
      }
    }
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
      const closePoint = step.map_close_point || panel.closePoint;
      const closed = await walkByMap(token, macro, {
        map_point: panel.toFraction(point),
        map_close_point: closePoint,
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
      // The server said no to the door: every retry just reloads the map (the
      // Map closes itself each time). Hand it back to the route to fight, or
      // log in again (Hà Đông 22,8, 2026-09-24 10:00).
      if (walk.refused) return { ...walk, note: `${walk.note}; ${summary()}` };
      // A Map click on a tile an NPC stands on is a click on that NPC: the
      // client walks up, touches it, and its popup then takes every later
      // click (Hà Đông 448, 21:25: 25,56 hit "Đại sứ càn quét" by 24,60).
      // Close that popup by its X and walk on (user, 2026-09-23).
      // Not a step taken since the Map click, nowhere near the target: the Map
      // never opened, so the X click landed on C.Phúc and Cầu phúc now covers
      // the game (Hà Đông 448 right after exit + reset, 2026-09-23 21:57).
      // User: if Cầu phúc opens, close it and go on - its X, from the user's
      // screenshot, at canvas (0.933, 0.095).
      // Only where the Map's X sits on C.Phúc (512-wide maps; on a 992-wide one
      // it is H.Động, 2026-09-23 22:28) and nothing is hitting us: monsters
      // around also keep us from moving, and then a click there opened H.Động.
      const stood = same(tileOf(after), tileOf(me));
      const near = Math.max(Math.abs(tileOf(after).x - tile.x), Math.abs(tileOf(after).y - tile.y)) <= 1;
      // Which top-bar panel a stray X opened, by the button under it: C.Phúc
      // on a 512-wide map, H.Động on a 992-wide one - each with its own X
      // (user's screenshots, 2026-09-23 / 24). Over H.Động's X, with no panel
      // open, sits Menu: hence only when nothing moved and nothing hits us.
      // H.Động is left out: its X sits over Menu, and a wrong guess opened the
      // N.Vật panel (2026-09-24 10:01). Door steps too: a refused door keeps
      // us standing still with no panel open at all.
      const strayPanel = !step.portal && [
        { name: "Cầu phúc", button: macro.blessing_button_point || [0.73, 0.07], close: macro.blessing_close_point || [0.933, 0.095] },
        ...(macro.activity_close_point
          ? [{ name: "H.Động", button: macro.activity_button_point || [0.905, 0.07], close: macro.activity_close_point }] : []),
      ].find((panel) => Math.abs(closePoint[0] - panel.button[0]) < 0.03 && Math.abs(closePoint[1] - panel.button[1]) < 0.04);
      const attacked = monstersNear(latestWorld, Number(macro.ambush_radius || 150), macro.ignore_monster_names)
        .some((monster) => monster.state & 2);
      // And only when the X really was clicked this leg: with it skipped (the
      // map reloaded) no panel opened, and a "close" there hit Menu instead
      // (Hà Đông 450, 2026-09-24 09:30: the N.Vật panel came up).
      const xClicked = closed === "nút X";
      if (stood && !near && xClicked && strayPanel && !attacked && !["map", "ambush"].includes(walk.how) && strayBlessings < 2) {
        strayBlessings += 1;
        clicks.push(await domClick(token, strayPanel.close));
        await domWait(token, 0.6);
        legs[legs.length - 1] += ` (không nhúc nhích: Map không mở, đóng ${strayPanel.name})`;
        continue;
      }
      const touched = (latestWorld?.touches || []).find((item) => item.at >= since);
      if (touched) {
        strayPopups += 1;
        if (strayPopups > Number(macro.stray_popup_limit ?? 3)) {
          throw new Error(`Bấm trên Map trúng NPC #${touched.target} ${strayPopups} lần; `
            + `đổi tọa độ bước "${step.label || step.goto.join(",")}" ra xa NPC. ${summary()}`);
        }
        await domWait(token, macro.popup_delay_seconds ?? 1);
        clicks.push(await domClick(token, macro.npc_popup_close_point || [0.842, 0.145]));
        await domWait(token, 0.6);
        legs[legs.length - 1] += ` (trúng NPC #${touched.target}, đóng popup)`;
        continue;
      }
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
        const through = await nudgeThrough();
        if (through) return through;
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
        const through = await nudgeThrough();
        if (through) return through;
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
    const through = await nudgeThrough();
    if (through) return through;
    return { how: "timeout", note: `chưa tới sau ${maxLegs} lượt; ${summary()}` };
  }

  // Attacks until no live monster is near us for a few checks in a row;
  // maxSeconds only caps a fight whose state cannot be read.
  // radius / idleRadius: a step can widen the macro's defaults to cover a whole
  // boss room (Cổ Mộ boss 1 must be left with nothing alive in it).
  // ignoreNames: replaces macro.ignore_monster_names for this step - a statue
  // that is only scenery out on the main map is a real target inside a room.
  // clearRoom: the room only finishes when it is empty, so none of the
  // give-up shortcuts apply - no dropping a target the game refuses, no
  // dropping one that will not lose health. maxSeconds is the only cap.
  async function fightUntilClear(token, macro, maxSeconds,
    { idle = true, radius, idleRadius, ignoreNames, clearRoom = false, minRounds = 0, bossAt = null } = {}) {
    const startedAt = Date.now();
    const deadline = startedAt + Number(maxSeconds) * 1000;
    const needed = Math.max(1, Number(macro.clear_checks_needed || 3));
    let rounds = 0;
    let streak = 0;
    let last = null;
    // Monsters the game refuses to hit (ATTACK_FAIL): 14 "Mục tiêu không nằm
    // trong tầm nhìn", 8 "Mục tiêu không thể tấn công" (e.g. a Mật thư), 3/4
    // dead or gone. Leave them and move on (user, 2026-09-14). In a clear_room
    // step only 3/4 (already dead) count: a cart or statue that must die for
    // the boss to appear is refused for a while before it can be hit, and
    // giving up on it leaves the room with no boss in it (user, 2026-09-21).
    const unhittable = clearRoom ? new Set([3, 4]) : new Set([3, 4, 8, 14]);
    // The game only sends ids; the name makes the log readable, so a room that
    // ends with no boss says what it refused to hit.
    const nameOf = (id) => (latestWorld?.creatures || []).find(([cid]) => cid === id)?.[5] || `#${id}`;
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
    const approached = new Set();        // idle ones clicked on before giving up
    // bossAt: where a boss stands that the unit list never shows (Thiên Long,
    // 2026-09-16). Skills alone never pick it: boss 5 at 77,64 went unhit from
    // 76,60 and the exit then stayed shut (user fought it by hand, 2026-09-24).
    // So click it on the main screen - the game makes it the target and walks
    // us up - at the start and again while no blow lands.
    let bossClickedAt = 0;
    let bossClicks = 0;
    const joinedAt = new Map();
    let health = new Map();
    let progressAt = startedAt;
    let hitting = false;
    let landed = null;
    const refusals = new Map();          // ATTACK_FAIL reason -> names refused
    const skipped = () => new Set([...untouched, ...unseen]);
    while (Date.now() < deadline) {
      if (macro.combat_check !== false) {
        for (const fail of latestWorld?.attackFails || []) {
          if (fail.at > startedAt && unhittable.has(fail.reason) && !unseen.has(fail.target)) {
            unseen.add(fail.target);
            refusals.set(fail.reason, [...(refusals.get(fail.reason) || []), nameOf(fail.target)]);
          }
        }
        last = combatState(macro, skipped(), { idle, radius, idleRadius, ignoreNames });
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
        const stalled = !clearRoom && now - progressAt >= stallMs;
        const drop = stalled
          ? near.filter((monster) => monster.hp >= 200 && now - joinedAt.get(monster.id) >= stallMs)
          : [];
        // Before dropping one, click it on the main screen once: skills do not
        // reach for an idle monster out of range, a click on it makes it the
        // target and walks us up (Cổ Mộ boss 2, 2026-09-23 23:18: stopped at
        // 43,31, the "bù nhìn" 118-206 px off, 25 rounds and not one hit).
        const fresh = drop.filter((monster) => !approached.has(monster.id)).sort((a, b) => a.d - b.d);
        const size = walkGridFor(latestWorld?.mapId)?.size;
        const aim = fresh.length && size && latestWorld?.me && macro.approach_idle_monsters !== false
          ? screenPointOf(macro, { x: fresh[0].x, y: fresh[0].y - 12 }, latestWorld.me, size) : null;
        if (aim && clearOfHud(aim)) {
          approached.add(fresh[0].id);
          await domClick(token, aim);
          progressAt = Date.now();
        } else if (drop.length) {
          for (const monster of drop) untouched.add(monster.id);
          last = combatState(macro, skipped(), { idle, radius, idleRadius, ignoreNames });
        }
        health = new Map((last.near || []).map((monster) => [monster.id, monster.hp]));
        streak = last.state === "clear" && !hitting ? streak + 1 : 0;
        if (streak >= needed && rounds >= minRounds) break;
        // No monster left: only wait out the checks. Pressing Đánh now picks
        // something that is no monster (the button turns into "Chat").
        // minRounds: skill rounds thrown anyway - at a door still shut after
        // logging in again, for a monster the list does not show (user,
        // 2026-09-23: "nếu vẫn không được thì đánh thêm vài cái").
        if (last.state === "clear" && !hitting && rounds >= minRounds) {
          await domDelay(macro.round_delay_seconds || 0.4);
          continue;
        }
      }
      const retargetMs = Number(macro.boss_retarget_seconds ?? 10) * 1000;
      if (bossAt && !hitting && Date.now() - bossClickedAt >= retargetMs && latestWorld?.me) {
        const size = walkGridFor(latestWorld?.mapId)?.size;
        const aim = size && screenPointOf(macro, bossAt, latestWorld.me, size);
        if (aim && clearOfHud(aim)) {
          await domClick(token, aim);
          bossClicks += 1;
        }
        bossClickedAt = Date.now();
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
      refusals: [...refusals]
        .map(([reason, who]) => `${reason}×${who.length} [${who.slice(0, 4).join(", ")}]`).join(" "),
      untouched: untouched.size,
      approached: approached.size,
      bossClicks,
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
      + (fight.approached ? `, bấm vào ${fight.approached} con đứng yên cho nhân vật lại gần` : "")
      + (fight.bossClicks ? `, bấm vào chỗ boss ${fight.bossClicks} lần` : "")
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
      // The server says no to a door in its own words: an ERROR reply to the
      // client's TOUCHEXIT (116). Hà Đông dễ, 2026-09-23 21:11: the door was
      // taken, the loading screen came, and then that reply - no map change -
      // while the flow kept clicking the door for minutes. Out of runs for
      // today ends the stage; anything else (monsters on us) is a shut door.
      const refusal = step.portal && (world.replies || [])
        .find((reply) => !reply.ok && reply.type === OP_TOUCH_EXIT && reply.at >= since);
      if (refusal) {
        if (/mỗi ngày/i.test(refusal.message)) {
          const error = new Error(`Game không cho qua cổng: ${refusal.message}`);
          error.dailyLimit = true;
          throw error;
        }
        return { how: "stuck", refused: true, note: `game không cho qua cổng: ${refusal.message}` };
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
  function combatState(macro, skipped = new Set(),
    { idle = true, radius: over, idleRadius: idleOver, ignoreNames } = {}) {
    const world = latestWorld;
    if (!world?.frames || !world.me) return { state: "unknown", detail: describeWorld() };
    const radius = Number(over ?? macro.monster_radius ?? 250);
    const around = monstersNear(world, radius, ignoreNames ?? macro.ignore_monster_names);
    // Monsters in the fight: attacking us (STATE_ATTACK) or wounded, and idle
    // full-health ones close by - a boss waits idle until hit (Thiên Long
    // trận). Ones that never die (e.g. "Tượng đá cơ quan") go on the ignore list.
    // idle false (clear-before-door steps): only attackers and wounded ones -
    // none left is our yellow name showing again, the door opens (user,
    // 2026-09-14).
    const idleRadius = Number(idleOver ?? macro.idle_monster_radius ?? 160);
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
    // In the fight: one attacking us (STATE_ATTACK) or already wounded.
    const engaged = (monster) => (monster.state & 2) || (monster.hp > 0 && monster.hp < 200);
    // The wall rule only ever applies to a monster minding its own business.
    // One that is ATTACKING us has plainly reached us, whatever the walk grid
    // says about the way round, and dropping it leaves the fight unfinished
    // while the door stays shut (user, 2026-09-23: Thiên Long's exit at 84,60
    // never opened - "bỏ 1 con sau tường" while that very monster was chasing
    // us at 157px with state 3).
    const reachable = (monster) => engaged(monster) || onFoot(monster);
    const walled = around.filter((monster) => !reachable(monster));
    const near = around.filter((monster) => reachable(monster) && !skipped.has(monster.id)
      && (engaged(monster) || (idle && monster.d <= idleRadius)));
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
        else if (macro.runner === "instance_reset_once") await runInstanceReset(token, macro, flow);
        else if (macro.runner === "dungeon_pipeline") await runDungeonPipeline(token, macro, flow);
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
