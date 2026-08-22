/*
 * VMGame — com/pip/ui/VMGame.java ported onto the JS VM.
 *
 * One VMGame wraps one running .etf script (a "screen"): its VM, its GWindows,
 * and its share of the frame loop. A VMGameManager holds the static registry
 * (sortHt keyed by vmKey, the widget table, the ETF cache) and drives
 * cycle/cycleUI/paint/packet dispatch exactly like the Java statics.
 *
 * Script loading is injected: `options.loadScript(name)` returns a Promise of
 * INFLATED etf bytes for "<name>.etf" (the caller handles gzip + fetch), which
 * keeps this module free of any I/O — Node tests use an in-memory map.
 *
 * Frame contract (call these once per frame, in this order):
 *   manager.cycle()          -> CYCLE (+CYCLEUI for the top UI)
 *   manager.handleSegment()  -> per incoming server packet
 *   manager.drawAll(g)       -> PAINT
 */
import { VM, INIT, CYCLE, PROCESSPACKET, CYCLEUI, PAINT, DESTROY } from './vm.js';
import { parseETF } from './etf.js';
import { CoreHost, composeHost, UNHANDLED } from './syscalls-core.js';
import { GfxHost, WidgetHost } from './syscalls-ui.js';
import { ResHost } from './syscalls-res.js';
import { SortHashtable } from './runtime.js';
import { UASegment } from './ua-segment.js';
import {
  GContainer, GWindow,
  GW_VM_JAVA_GWIDGET, GW_VM_XX, GW_VM_YY, GW_VM_W, GW_VM_H,
  GW_VM_FUNC_PAINT,
} from './widgets.js';
import { rectIn } from './tool.js';

export const CONN_VM_DATA = -10000;
export const CONN_VM_COMMAND = -10001;
/** Tool.CONN_SYNC_SERVER — the engine's own clock-sync packet, never a script's. */
export const CONN_SYNC_SERVER = 102;

export const VM_TYPE_GAME = 0;
export const VM_TYPE_UI = 1;
export const VM_TYPE_LIB = 2;

export const STATE_IDLE = -1;
export const STATE_REQUESTING_VMUI = 1;

export const CALLBACK_LOAD_ETF_START = 'LoadEtfStart';
export const CALLBACK_LOAD_ETF_END = 'LoadEtfEnd';
export const CALLBACK_LOAD_ETF_END1 = 'LoadEtfEnd1';
export const CALLBACK_VMGAME_DESTROY_NOTIFY = 'VMGameDestroyNotify';

/**
 * Stubs for the GameWorld.panel item registry (the 0x56xx game_panel_* family)
 * and VMCounter (0x3061..). The real panel renderer lands with the world
 * layer; scripts only need stable ids back during boot.
 */
class PanelStub {
  constructor() {
    this.nextId = 1;
    this.images = [];
    this.items = new Map();
  }

  regImage(imageSet) {
    this.images.push(imageSet);
    return this.images.length - 1;
  }

  releaseImage(index) {
    this.images[index] = null;
  }

  addItem(kind) {
    const id = this.nextId++;
    this.items.set(id, kind);
    return id;
  }

  removeItem(id) {
    this.items.delete(id);
  }

  clearItems() {
    this.items.clear();
  }
}

/** VMCounter — named countdown timers keyed by int. */
class VMCounter {
  constructor() {
    /** @type {Map<number, {deadline:number, duration:number}>} */
    this.timers = new Map();
  }

  set(key, durationMs) {
    this.timers.set(key, { deadline: Date.now() + durationMs, duration: durationMs });
  }

  getSaveTimeSec(key) {
    const t = this.timers.get(key);
    if (!t) return -1;
    return Math.max(0, Math.ceil((t.deadline - Date.now()) / 1000));
  }

  getSaveTimeMillis(key) {
    const t = this.timers.get(key);
    if (!t) return -1;
    return Math.max(0, t.deadline - Date.now());
  }

  remove(key) {
    this.timers.delete(key);
  }

  removeAll() {
    this.timers.clear();
  }
}

/**
 * Minimal stand-in for the Java GameWorld processor (com.pip.sanguo.GameWorld):
 * the object scripts reach with GetWorldProc/ReadWorldData/SaveGameData. The
 * real world state arrives with the G3d world layer; until then this is a
 * named data store so the boot path runs.
 */
export class WorldProcessor {
  constructor() {
    /** @type {Map<string, any>} */
    this.data = new Map();
  }

  readGameData(name) {
    return this.data.get(name) ?? null;
  }

  saveGameData(name, value) {
    this.data.set(name, value);
  }

  removeGameData(name) {
    this.data.delete(name);
  }
}

let vmKeyMaker = 0;

/**
 * Extra syscall ids only the VMGame layer can answer (window registry lookups,
 * UI lifecycle). Returns UNHANDLED for everything else.
 */
class VmGameHost {
  constructor(manager) {
    this.manager = manager;
  }

  handle(vm, funcID, params) {
    const owner = vm.owner;
    const M = this.manager;
    switch (funcID) {
      case 0x1100: owner.setCatchInput(params[0] === 1); return 0;           // SetUICatchInput
      case 0x1101: owner.setTransparent(params[0] === 1); return 0;          // SetUITransparent
      case 0x1102: owner.close(); return 0;                                  // CloseUI
      case 0x1103: return vm.makeTempObject(vm.followPointer(params[0])?.getVMId() ?? null);
      case 0x1104: owner.setSingleton(params[0] === 1); return 0;
      case 0x1105: return vm.makeTempObject(M.getVMGameByVMKey(params[0]));
      case 0x1106: return vm.makeTempObject(M.getVMGame(String(vm.followPointer(params[0]) ?? '')));
      case 0x1108: return vm.followPointer(params[0])?.getSingleton() ? 1 : 0;
      case 0x1109: return M.getCommonKey();
      case 0x110a: M.openUI(String(vm.followPointer(params[0]) ?? ''), null); return 0; // OpenUI (async fire-and-forget)
      case 0x110b: M.closeAllUI(owner, params[0]); return 0;
      case 0x1200: M.loadVMGame(String(vm.followPointer(params[0]) ?? ''), VM_TYPE_GAME, true); return 0;
      case 0x1201: M.removeVMGame(String(vm.followPointer(params[0]) ?? '')); return 0;
      case 0x1202: return vm.makeTempObject(M.world); // GetWorldProc
      case 0x1205: { // ReadGameData(processor, name)
        const proc = vm.followPointer(params[0]);
        const d = proc?.readGameData?.(String(vm.followPointer(params[1]) ?? '')) ?? null;
        if (d instanceof UASegment) { d.flush(); d.reset(); }
        return vm.makeTempObject(d);
      }
      case 0x1206: { // SaveGameData(processor, name, data)
        const proc = vm.followPointer(params[0]);
        const d = vm.followPointer(params[2]);
        if (d instanceof UASegment) { d.flush(); d.reset(); }
        proc?.saveGameData?.(String(vm.followPointer(params[1]) ?? ''), d);
        return 0;
      }
      case 0x1207: { // RemoveGameData(processor, name)
        vm.followPointer(params[0])?.removeGameData?.(String(vm.followPointer(params[1]) ?? ''));
        return 0;
      }
      case 0x120c: { // ReadWorldData(name)
        const d = M.world.readGameData(String(vm.followPointer(params[0]) ?? ''));
        if (d instanceof UASegment) { d.flush(); d.reset(); }
        return vm.makeTempObject(d);
      }
      case 0x1208: { // RealizeVMData(data) -> key in the global VM-data table
        const key = M.globalVMDataNext++;
        M.globalVMData.set(key, vm.followPointer(params[0]));
        return key;
      }
      case 0x1209: // ReadVMData(key)
        return vm.makeTempObject(M.globalVMData.get(params[0]) ?? null);
      case 0x120a: // FreeVMData(key)
        M.globalVMData.delete(params[0]);
        return 0;
      case 0x1301: { // ResSetGameConst(numberImage, needCacheVm[], battleRemind[],
                     //                javaWorldPacket[], intConst[])
        // GameMain statics the engine needs to route packets and draw numbers.
        M.gameConst.numberImage = vm.followPointer(params[0]);
        M.gameConst.needCacheVm = new Set(vm.followPointer(params[1]) ?? []);
        M.gameConst.battleRemind = vm.followPointer(params[2]) ?? null;
        M.worldPacketOpcodes = new Set(vm.followPointer(params[3]) ?? []);
        M.gameConst.intConst = vm.followPointer(params[4]) ?? [];
        return 0;
      }
      case 0x1337: // ResSetTeamInfo(Hashtable) — team panel state lands later
        M.worldState.teamInfo = vm.followPointer(params[0]);
        return 0;
      case 0x5545: // vm_game_set_collision_test_para(maxStep, stepAdd)
        M.worldState.collisionMaxStep = params[0];
        M.worldState.collisionStepAdd = params[1];
        return 0;
      case 0x5506: // vm_world_set_mini_map_config(seg) — minimap lands with the world layer
        M.worldState.miniMapConfig = vm.followPointer(params[0]);
        return 0;

      /* ------------------------------------- game_panel item registry */
      case 0x5602: return M.panel.regImage(vm.followPointer(params[0])); // reg_image
      case 0x5603: M.panel.releaseImage(params[0]); return 0;            // release_image
      // every add_item_* just needs a unique id until the panel renders
      case 0x5604: case 0x5605: case 0x5606: case 0x5607: case 0x5608:
      case 0x5609: case 0x560a: case 0x560b: case 0x560c: case 0x560d:
      case 0x560e: case 0x560f: case 0x5610: case 0x5611: case 0x5612:
      case 0x5615: case 0x5617: case 0x5618: case 0x5619: case 0x561a:
      case 0x561b: case 0x561c: case 0x561d: case 0x561e: case 0x561f:
      case 0x5620: case 0x5621: case 0x5622: case 0x5623: case 0x5624:
      case 0x5625: case 0x5626: case 0x5627: case 0x5628: case 0x5629:
      case 0x5633: case 0x5634: case 0x5635: case 0x5636: case 0x5637:
        return M.panel.addItem(funcID);
      case 0x5613: M.panel.removeItem(params[0]); return 0;              // remove_item
      case 0x5614: M.panel.clearItems(); return 0;                       // clear_item

      /* --------------------------------------------------- VMCounter */
      case 0x3061: return M.counter.getSaveTimeSec(params[0]);
      case 0x3062: return M.counter.getSaveTimeMillis(params[0]);
      case 0x3063: M.counter.remove(params[0]); return 0;
      case 0x3064: M.counter.removeAll(); return 0;
      case 0x1211: return vm.makeTempObject(M.getVMParam(String(vm.followPointer(params[0]) ?? '')));
      case 0x1212: M.openUI(String(vm.followPointer(params[0]) ?? ''), vm.followPointer(params[1])); return 0;
      case 0x1213: owner.addCommonCallback(CYCLE, params); return 0;
      case 0x1214: owner.addCommonCallback(CYCLEUI, params); return 0;
      case 0x1215: owner.addCommonCallback(PAINT, params); return 0;
      case 0x1216: owner.addCommonCallback(PROCESSPACKET, params); return 0;
      case 0x1217: owner.removeCommonCallback(CYCLE, params); return 0;
      case 0x1218: owner.removeCommonCallback(CYCLEUI, params); return 0;
      case 0x1219: owner.removeCommonCallback(PAINT, params); return 0;
      case 0x1220: owner.removeCommonCallback(PROCESSPACKET, params); return 0;
      case 0x1278: return vm.makeTempObject(M.getTopGWindow());
      case 0x1279: { // GetPointerGWidgetInWin
        const win = M.widgetFromPtr(vm, params[0]);
        const w = win instanceof GWindow ? win.searchWidget(params[1], params[2]) : null;
        return w != null ? vm.makeTempObject(w.vmData) : 0;
      }
      case 0x127a: { // GetPointerGWidget
        const w = M.getPointerWidget(params[0], params[1]);
        return w != null ? vm.makeTempObject(w.vmData) : 0;
      }
      case 0x12b3: { // GetMouseTopGWindow
        const w = M.getMouseTopGWindow(params[0], params[1]);
        return w != null ? vm.makeTempObject(w.vmData) : 0;
      }
      case 0x3082: M.closeVM(String(vm.followPointer(params[0]) ?? '')); return 0;
      case 0x3083: M.closeVM(params[0]); return 0;
      case 0x3085: return vm.makeTempObject(M.vmGames);
      case 0x563d: M.setToTop(String(vm.followPointer(params[0]) ?? '')); return 0;
      case 0x570e: return vm.makeTempObject(M.getTopUIVM());
      default:
        return UNHANDLED;
    }
  }
}

export class VMGame {
  /**
   * @param {string} vmId
   * @param {Uint8Array} etfData INFLATED etf bytes
   * @param {number} vmType VM_TYPE_* (libs skip INIT)
   * @param {VMGameManager} manager
   * @param {object} platform platform hooks shared by every VMGame's host
   */
  constructor(vmId, etfData, vmType, manager, platform) {
    this.vmId = vmId;
    this.vmType = vmType;
    this.manager = manager;
    this.catchInput = vmType === VM_TYPE_UI;
    this.isSingleton = true;
    this.closed = false;
    this.transparent = false;
    this.oldVMsgPanelY = -1000;

    /** @type {Map<number, GWidget>} widgets created by THIS script */
    this.vmGWidgets = new Map();

    /** @type {SortHashtable<GWindow>} insertion-ordered window stack */
    this.vmContainers = new SortHashtable();

    const core = new CoreHost(platform);
    const gfx = new GfxHost(platform);
    const widgets = new WidgetHost(platform);
    const res = new ResHost(manager.resources);
    const gameLayer = new VmGameHost(manager);
    const host = composeHost(core.platform, core, gfx, widgets, res, gameLayer);

    this.gtvm = new VM({ owner: this, host, onError: platform.onError });
    this.gtvm.init(parseETF(etfData));
    // dev/test budget: a runaway script must fail loudly, not freeze the tab
    this.gtvm.instructionLimit = platform.instructionLimit ?? 0;
    this.gtvm.link((libName) => {
      const lib = manager.getVMGame(libName);
      if (!lib || !lib.gtvm) throw new Error(`unresolved library "${libName}" for ${vmId}`);
      return lib.gtvm;
    });

    this.vmKey = ++vmKeyMaker;
    if (vmId === 'game_world') {
      manager.gameWorldVMGameKey = this.vmKey;
    }

    if (vmType !== VM_TYPE_LIB) {
      this.gtvm.execute(INIT);
    }
  }

  /* ------------------------------------------------------- widgets */

  putGWidget(gWidget) {
    if (gWidget != null) {
      const key = gWidget.vmData[GW_VM_JAVA_GWIDGET];
      this.manager.gWidgets.set(key, gWidget);
      this.vmGWidgets.set(key, gWidget);
    }
  }

  removeGWidget(gWidget) {
    if (gWidget != null) {
      const key = gWidget.vmData[GW_VM_JAVA_GWIDGET];
      this.manager.gWidgets.delete(key);
      this.vmGWidgets.delete(key);
    }
  }

  /** VMGame.getGWidget — the widget table is global, keyed by GW_VM_JAVA_GWIDGET */
  getGWidget(key) {
    return this.manager.gWidgets.get(key) ?? null;
  }

  /* ------------------------------------------------------- windows */

  createWindow(vmObj, vmData, isTransparent, name) {
    const window = new GWindow(this, vmObj, vmData, isTransparent, name);
    this.vmContainers.put(window, window);
    window.isShow = false;
    return window;
  }

  vmDestroyWindow(gWindow) {
    this.vmContainers.remove(gWindow);
    gWindow.destroy();
  }

  vmCloseWindow(gWindow) {
    gWindow.isShow = false;
  }

  vmShowWindow(gWindow) {
    this.vmContainers.remove(gWindow);
    this.vmContainers.put(gWindow, gWindow);
    gWindow.isShow = true;
  }

  getGWindows() {
    return this.vmContainers;
  }

  /* ---------------------------------------------- common callbacks */

  addCommonCallback(type, paramsOrWin, funcName) {
    // two shapes: syscall form (winPtrParams, funcNamePtrIndex) and direct
    let gWindow;
    let name;
    if (paramsOrWin instanceof GWindow) {
      gWindow = paramsOrWin;
      name = funcName;
    } else {
      // params = [self, ..., gWindowPtr?, ...]; the syscall passes
      // (gWindow ptr at params[0], name ptr at params[1])
      gWindow = this.manager.widgetFromPtr(this.gtvm, paramsOrWin[0]);
      name = String(this.gtvm.followPointer(paramsOrWin[1]) ?? '');
    }
    if (!(gWindow instanceof GWindow)) return;
    switch (type) {
      case CYCLE: gWindow.funcCycle = name; break;
      case CYCLEUI: gWindow.funcCycleUI = name; break;
      case PAINT: gWindow.funcPaint = name; break;
      case PROCESSPACKET: gWindow.funcPacket = name; break;
    }
  }

  removeCommonCallback(type, paramsOrWin) {
    let gWindow;
    if (paramsOrWin instanceof GWindow) {
      gWindow = paramsOrWin;
    } else {
      gWindow = this.manager.widgetFromPtr(this.gtvm, paramsOrWin[0]);
    }
    if (!(gWindow instanceof GWindow)) return;
    switch (type) {
      case CYCLE: gWindow.funcCycle = null; break;
      case CYCLEUI: gWindow.funcCycleUI = null; break;
      case PAINT: gWindow.funcPaint = null; break;
      case PROCESSPACKET: gWindow.funcPacket = null; break;
    }
  }

  processCommonCallback(type) {
    const containerCount = this.vmContainers.size();
    if (containerCount <= 0) return;

    const values = this.vmContainers.values();
    // top-most window downward to the first opaque one with a PAINT func
    let firstIndex = containerCount - 1;
    while (firstIndex >= 0) {
      const container = values[firstIndex];
      if (container == null) break;
      if (type === PROCESSPACKET || container.isTransparent ||
          container.vmData[GW_VM_FUNC_PAINT] === 0) {
        firstIndex--;
      } else {
        break;
      }
    }
    if (firstIndex < 0) firstIndex = 0;

    if (type === CYCLEUI) {
      for (let i = containerCount - 1; i >= firstIndex; i--) {
        const gWindow = values[i];
        this.handleCaller(gWindow, type);
        if (gWindow.isShow && gWindow.catchInput) {
          this.manager.platform.clearKeys?.();
        }
      }
    } else {
      for (let i = firstIndex; i < containerCount; i++) {
        this.handleCaller(values[i], type);
      }
    }
  }

  handleCaller(gWindow, type) {
    if (gWindow == null || ((type === CYCLE || type === CYCLEUI || type === PAINT) && gWindow.isShow === false)) {
      return;
    }
    gWindow.handleCaller(type, this.gtvm.blocked);
  }

  /* ---------------------------------------------------- frame loop */

  vmCycle() {
    if (this.closed) return;
    if (this.gtvm != null) {
      this.processCommonCallback(CYCLE);
      this.gtvm.execute(CYCLE);
    }
  }

  cycleUI() {
    if (this.closed) return;
    if (this.gtvm != null) {
      this.processCommonCallback(CYCLEUI);
      this.gtvm.execute(CYCLEUI);
    }
  }

  /** Java callback(String, Object[]) — boxes Objects into temp pointers. */
  callback(funcName, data) {
    const vm = this.gtvm;
    if (vm == null) return -1;
    let params = null;
    if (data != null) {
      params = new Int32Array(data.length);
      for (let i = 0; i < data.length; i++) {
        params[i] = typeof data[i] === 'number' ? data[i] | 0 : vm.makeTempObject(data[i]);
      }
    }
    return vm.callback(funcName, params);
  }

  draw(g) {
    if (this.closed) return;
    if (!this.transparent && !GWindow.forcePaintWorld) {
      this.manager.platform.drawDefaultBackground?.(g);
    }
    if (this.gtvm != null) {
      this.gtvm.execute(PAINT);
      this.processCommonCallback(PAINT);
    }
  }

  /* ---------------------------------------------------- lifecycle */

  close() {
    this.closed = true;
  }

  isClosed() {
    return this.closed;
  }

  isTransparent() {
    return this.transparent;
  }

  setTransparent(t) { this.transparent = t; }
  setSingleton(s) { this.isSingleton = s; }
  getSingleton() { return this.isSingleton; }
  isCatchInput() { return this.catchInput; }
  setCatchInput(c) { this.catchInput = c; }
  getVMType() { return this.vmType; }
  getVMId() { return this.vmId; }
  getVM() { return this.gtvm; }

  destroy() {
    if (this.gtvm != null) {
      this.gtvm.execute(DESTROY);
      this.gtvm = null;
      this.manager.vmGames.remove(this.vmKey);

      const gvm = this.manager.getVMGameByVMKey(this.manager.gameWorldVMGameKey);
      if (gvm != null && gvm.gtvm != null) {
        gvm.gtvm.callback(CALLBACK_VMGAME_DESTROY_NOTIFY,
          [this.vmKey, gvm.gtvm.makeTempObject(gvm), gvm.gtvm.makeTempObject(this.vmId), this.vmType]);
      }
      this.vmGWidgets.clear();
    }
  }
}

/**
 * The static side of VMGame.java: the registry, the ETF cache and the frame
 * entry points. One instance per client.
 */
export class VMGameManager {
  /**
   * @param {object} options
   * @param {object} platform  shared platform hooks (see syscalls-core.js)
   * @param {(name: string) => Promise<Uint8Array>} loadScript
   *        inflated .etf bytes for a script/library name
   */
  constructor({ platform = {}, loadScript, resources = null }) {
    this.platform = platform;
    this.loadScript = loadScript;
    /** @type {import('./resources.js').ResourceStore|null} shared asset cache */
    this.resources = resources;
    /** @type {SortHashtable<VMGame>} vmKey -> VMGame, insertion = z-order */
    this.vmGames = new SortHashtable();
    /** @type {Map<string, Uint8Array>} inflated etf cache */
    this.vmCache = new Map();
    /** @type {Map<string, any>} server-pushed open parameters */
    this.vmParams = new Map();
    /** @type {Map<number, GWidget>} */
    this.gWidgets = new Map();
    /** @type {Map<string, number>} pending loads: id -> started-at ms */
    this.loadingVMID = new Map();
    /** the world processor scripts reach with GetWorldProc (0x1202) */
    this.world = new WorldProcessor();
    /** VM.java's static globalVMData table (RealizeVMData/ReadVMData) */
    this.globalVMData = new Map();
    this.globalVMDataNext = 1;
    /** misc world constants scripts set during boot (team info, collision...) */
    this.worldState = {};
    /** ResSetGameConst (0x1301): the GameMain statics the scripts configure */
    this.gameConst = { numberImage: null, needCacheVm: new Set(), battleRemind: null, intConst: [] };
    /** opcodes the JAVA world layer owns (0x1301) — they never reach the scripts */
    this.worldPacketOpcodes = new Set();
    /** the packet currently being dispatched — what GetNextPacket (0x0089) returns */
    this.currentPacket = null;
    /** GameWorld.panel stand-in (game_panel_* item registry) */
    this.panel = new PanelStub();
    /** VMCounter timers */
    this.counter = new VMCounter();
    this.state = STATE_IDLE;
    this.gameWorldVMGameKey = 0;
    this.commonKey = 0;
  }

  /* ------------------------------------------------------ widgets */

  widgetFromPtr(gtvm, ptr) {
    const data = gtvm.followPointer(ptr);
    if (data == null) return null;
    return this.gWidgets.get(data[GW_VM_JAVA_GWIDGET]) ?? null;
  }

  getGWidget(key) {
    return this.gWidgets.get(key) ?? null;
  }

  /* ----------------------------------------------------- registry */

  getVMGameByVMKey(vmKey) {
    return this.vmGames.get(vmKey) ?? null;
  }

  getVMGame(vmId) {
    for (const vg of this.vmGames.values()) {
      if (vg.vmId === vmId) return vg;
    }
    return null;
  }

  getLastVMGame(vmId) {
    let ret = null;
    for (const vg of this.vmGames.values()) {
      if (vg.vmId === vmId) ret = vg;
    }
    return ret;
  }

  getCommonKey() {
    return ++this.commonKey;
  }

  getVMParam(vmId) {
    const p = this.vmParams.get(vmId);
    this.vmParams.delete(vmId);
    return p ?? null;
  }

  setToTop(vmId) {
    const vg = this.getLastVMGame(vmId);
    if (vg == null) return;
    this.vmGames.remove(vg.vmKey);
    this.vmGames.put(vg.vmKey, vg);
  }

  /* ------------------------------------------------------ loading */

  /**
   * loadVMGame — resolves immediately when the bytes are cached/local,
   * otherwise requests them and parks the frame loop in STATE_REQUESTING_VMUI
   * until they arrive (recvEtfData).
   * @returns {Promise<number>} the new vmKey
   */
  async loadVMGame(vmId, vmType = VM_TYPE_UI, sync = true) {
    const existing = this.getVMGame(vmId);
    if (existing != null && existing.isSingleton) {
      existing.close();
      existing.destroy();
    }

    let etfData = this.vmCache.get(vmId);
    if (etfData == null) {
      try {
        etfData = await this.loadScript(vmId);
      } catch {
        etfData = null;
      }
      if (etfData == null) {
        if (vmType === VM_TYPE_UI) {
          this.loadingVMID.set(vmId, Date.now());
          if (this.state === STATE_IDLE) {
            this.state = STATE_REQUESTING_VMUI;
            const gw = this.getVMGame('game_world');
            gw?.gtvm?.callback(CALLBACK_LOAD_ETF_START, [vmId]);
          }
        }
        return 0;
      }
    }

    return this.addUI(vmId, etfData, vmType);
  }

  /** recvEtfData — a requested script arrived from the server. */
  recvEtfData(vmId, etfData) {
    let etfVMKey = 0;
    try {
      etfVMKey = this.addUI(vmId, etfData, VM_TYPE_UI);
      const gameWorld = this.getVMGame('game_world');
      gameWorld?.gtvm?.callback(CALLBACK_LOAD_ETF_END1, [vmId, etfVMKey]);
    } catch (err) {
      console.error(`[vmgame] recvEtfData ${vmId}:`, err);
    }
    this.loadingVMID.delete(vmId);
    this.checkLoading();
  }

  checkLoading() {
    if (this.loadingVMID.size === 0) {
      this.state = STATE_IDLE;
      const gw = this.getVMGame('game_world');
      gw?.gtvm?.callback(CALLBACK_LOAD_ETF_END, null);
    }
  }

  /** addUI — instantiate (loading libraries recursively), run INIT, register. */
  addUI(vmId, etfData, vmType) {
    this.vmCache.set(vmId, etfData);

    // load every library this script links against that is not running yet
    const probe = parseETF(etfData);
    for (const libName of probe.libNames) {
      if (this.getVMGame(libName) != null) continue;
      // synchronous path requires the lib to already be cached; otherwise the
      // caller must preload it (the boot sequence does)
      const libData = this.vmCache.get(libName);
      if (libData == null) {
        throw new Error(`library "${libName}" not preloaded for ${vmId}`);
      }
      this.addUI(libName, libData, VM_TYPE_LIB);
    }

    const mn = new VMGame(vmId, etfData, vmType, this, this.platform);
    this.vmGames.put(mn.vmKey, mn);
    return mn.vmKey;
  }

  removeVMGame(vmId) {
    const vg = this.getLastVMGame(vmId);
    if (vg != null) {
      vg.gtvm.execute(DESTROY);
      vg.destroy();
    }
  }

  openUI(vmId, param) {
    this.vmParams.set(vmId, param ?? null);
    return this.loadVMGame(vmId, VM_TYPE_UI, true);
  }

  closeVM(vmIdOrKey) {
    const vg = typeof vmIdOrKey === 'string'
      ? this.getVMGame(vmIdOrKey)
      : this.getVMGameByVMKey(vmIdOrKey);
    vg?.close();
  }

  static closeAllUI(refUI, type) {
    // implemented as an instance helper below; kept static-shaped for parity
  }

  closeAllUI(refUI, type) {
    for (const vg of [...this.vmGames.values()]) {
      if (vg.getVMType() !== VM_TYPE_UI) continue;
      if (type === 1 && vg === refUI) break;
      vg.close();
    }
  }

  hasUI(uiID) {
    const vg = this.getVMGame(uiID);
    if (vg != null && vg.vmId === uiID && !vg.isClosed()) return true;
    return this.loadingVMID.has(uiID);
  }

  /* --------------------------------------------------- frame loop */

  cycle() {
    if (this.state === STATE_REQUESTING_VMUI) {
      const now = Date.now();
      for (const [id, start] of this.loadingVMID) {
        if (now - start >= 15000) this.loadingVMID.delete(id); // TIME_SEND_TIMEOUT
      }
      this.checkLoading();
      return;
    }

    const vmcount = this.vmGames.size();
    if (this.state === STATE_IDLE && vmcount > 0) {
      const values = this.vmGames.values();
      for (let i = vmcount - 1; i >= 0; i--) {
        const mn = values[i];
        switch (mn.vmType) {
          case VM_TYPE_GAME:
            mn.vmCycle();
            break;
          case VM_TYPE_UI:
            try {
              mn.vmCycle();
            } catch (err) {
              console.error(`[vmgame] error ui ${mn.getVMId()}:`, err);
              mn.close();
            }
            if (this.isTopUI(mn.vmId)) {
              try {
                mn.cycleUI();
              } catch (err) {
                console.error(`[vmgame] error cycleUI ${mn.getVMId()}:`, err);
                mn.close();
              }
            }
            if (mn.isClosed()) mn.destroy();
            break;
        }
      }
    }
  }

  isTopUI(vmId) {
    const vg = this.getLastVMGame(vmId);
    if (vg == null) return false;
    const values = this.vmGames.values();
    const startIndex = this.vmGames.index(vg.vmKey);
    if (startIndex < 0) return false;
    for (let i = startIndex + 1; i < values.length; i++) {
      const mn = values[i];
      if (mn.vmType === VM_TYPE_UI && mn.isCatchInput()) return false;
    }
    return true;
  }

  /**
   * handleIncoming — GameMain.handleSegment (line 1030) ported: the engine's
   * clock-sync packet never reaches scripts; opcodes the Java world layer
   * registered (0x1301) go to the world renderer; everything else is offered
   * to the scripts, and if none of them handles it, the world gets a turn.
   * While a script runs, `currentPacket` is what GetNextPacket (0x0089) hands
   * back — exactly the Java static `nextPacket`.
   * @param {import('./ua-segment.js').UASegment} segment
   * @param {() => void} [worldProcessPacket] the world renderer's fallback
   * @returns {boolean} true when a script handled the packet
   */
  handleIncoming(segment, worldProcessPacket = null) {
    if (segment.type === CONN_SYNC_SERVER) return false;
    this.currentPacket = segment;
    try {
      if (this.worldPacketOpcodes.has(segment.type)) {
        worldProcessPacket?.();
        return false;
      }
      this.handleSegment(segment);
      if (!segment.handled) worldProcessPacket?.();
      return segment.handled;
    } finally {
      this.currentPacket = null;
    }
  }

  /** handleSegment — offer one incoming packet to every live script, top-down. */
  handleSegment(segment) {
    const values = this.vmGames.values();
    for (let i = values.length - 1; i >= 0; i--) {
      const vg = values[i];
      if (vg.vmType === VM_TYPE_LIB || vg.closed) continue;
      segment.reset();
      vg.gtvm.execute(PROCESSPACKET);
      if (segment.handled) return;
      vg.processCommonCallback(PROCESSPACKET);
      if (segment.handled) return;
    }
  }

  /** drawAll — bottom-up over the UI stack, games always paint. */
  drawAll(g) {
    const uiCount = this.vmGames.size();
    const values = this.vmGames.values();
    let firstIndex = uiCount - 1;
    while (firstIndex >= 0) {
      const mn = values[firstIndex];
      if (mn == null) break;
      if (mn.isTransparent() || mn.vmType !== VM_TYPE_UI) firstIndex--;
      else break;
    }
    if (firstIndex < 0) firstIndex = 0;
    while (firstIndex < uiCount) {
      const mn = values[firstIndex];
      if (mn.vmType === VM_TYPE_UI) mn.draw(g);
      else if (mn.vmType === VM_TYPE_GAME) mn.gtvm.execute(PAINT);
      firstIndex++;
    }
  }

  /* -------------------------------------------------- hit testing */

  getTopUIVMId() {
    const values = this.vmGames.values();
    for (let i = values.length - 1; i >= 0; i--) {
      if (values[i].vmType === VM_TYPE_UI) return values[i].vmId;
    }
    return null;
  }

  getTopUIVM() {
    const values = this.vmGames.values();
    for (let i = values.length - 1; i >= 0; i--) {
      if (values[i].vmType === VM_TYPE_UI) return values[i];
    }
    return null;
  }

  getTopGWindow() {
    const vgs = this.vmGames.values();
    for (let i = vgs.length - 1; i >= 0; i--) {
      const vg = vgs[i];
      if (vg.vmContainers != null && vg.vmContainers.size() > 0) {
        const wins = vg.vmContainers.values();
        for (let j = wins.length - 1; j >= 0; j--) {
          if (wins[j].isShow) return wins[j];
        }
      }
    }
    return null;
  }

  getMouseTopGWindow(x, y) {
    const vgs = this.vmGames.values();
    for (let i = vgs.length - 1; i >= 0; i--) {
      const vg = vgs[i];
      if (vg.vmContainers != null && vg.vmContainers.size() > 0) {
        const wins = vg.vmContainers.values();
        for (let j = wins.length - 1; j >= 0; j--) {
          const w = wins[j];
          if (w.isShow) {
            if (rectIn(w.vmData[GW_VM_XX], w.vmData[GW_VM_YY], w.vmData[GW_VM_W], w.vmData[GW_VM_H], x, y)) {
              return w;
            }
            if (w.catchInput) return w;
          }
        }
      }
    }
    return null;
  }

  getPointerWidget(x, y) {
    const top = this.getTopGWindow();
    return top != null ? top.searchWidget(x, y) : null;
  }
}