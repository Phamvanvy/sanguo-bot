const API = "http://127.0.0.1:8765/api";
const flowsNode = document.querySelector("#flows");
const statusNode = document.querySelector("#status");
const connectionNode = document.querySelector("#connection");
const stopButton = document.querySelector("#stop");
let flows = [];

async function request(path, options = {}) {
  const response = await fetch(`${API}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.error || "Controller error");
  return data;
}

function setConnected(value) {
  connectionNode.className = `dot ${value ? "online" : "offline"}`;
}

function renderFlows(running) {
  flowsNode.replaceChildren(...flows.map((flow) => {
    const button = document.createElement("button");
    button.className = "flow";
    button.type = "button";
    button.disabled = running;
    button.innerHTML = `<span class="flow-icon"></span><strong></strong><small></small>`;
    button.querySelector(".flow-icon").textContent = flow.icon;
    button.querySelector("strong").textContent = flow.label;
    button.querySelector("small").textContent = flow.description;
    button.addEventListener("click", () => runFlow(flow.id));
    return button;
  }));
}

async function runFlow(flow) {
  try {
    const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
    if (!tab?.title || !tab?.url) throw new Error("Không đọc được tab đang chọn");
    if (new URL(tab.url).origin !== "https://play.minhchauh5.com") {
      throw new Error("Hãy mở flow từ tab https://play.minhchauh5.com/");
    }
    const data = await request("/run", {
      method: "POST",
      body: JSON.stringify({ flow, tabTitle: tab.title, tabUrl: tab.url }),
    });
    showStatus(data);
  } catch (error) {
    statusNode.textContent = error.message;
  }
}

async function stopFlow() {
  try { showStatus(await request("/stop", { method: "POST", body: "{}" })); }
  catch (error) { statusNode.textContent = error.message; }
}

function showStatus(data) {
  const running = data.state === "running";
  statusNode.classList.toggle("running", running);
  statusNode.textContent = running ? `Đang chạy: ${data.flow}` : (data.message || "Sẵn sàng");
  stopButton.disabled = !running;
  renderFlows(running);
}

async function refresh() {
  try {
    if (!flows.length) flows = (await request("/flows")).flows;
    setConnected(true);
    showStatus(await request("/status"));
  } catch (_) {
    setConnected(false);
    statusNode.classList.remove("running");
    statusNode.textContent = "Controller chưa chạy. Mở start-extension-server.ps1";
    stopButton.disabled = true;
    renderFlows(true);
  }
}

stopButton.addEventListener("click", stopFlow);
refresh();
setInterval(refresh, 1200);
