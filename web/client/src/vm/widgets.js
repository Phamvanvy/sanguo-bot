/*
 * The widget toolkit — com/pip/gui/{GWidget,GContainer,GWindow,GScrollBar}.java
 * as the .etf scripts drive it through the 0x12xx syscall block.
 *
 * Fidelity contract:
 *  - vmData is an Int32Array laid out EXACTLY like the Java one (GW_* below);
 *    scripts read/write those slots directly, so indices are load-bearing.
 *  - Every script-visible behaviour (bounds clamping, realize of GW_VM_SELF,
 *    z-order on add, out-of-view marking during V layout, scroll bookkeeping,
 *    the call stacks handleCaller builds) follows the Java source call-for-call.
 *
 * A widget holds a reference to its VMGame, which must provide:
 *   gtvm            — the VM (callback/makeTempObject/followPointer/free/getRealizeAdrr)
 *   putGWidget(gw) / removeGWidget(gw)
 *   viewWidth/viewHeight          (GameMain.viewWidth/viewHeight)
 *   destroyWindow?(gw)            (used by GContainer.destroy on windows)
 */
import { rectIntersect, rectContain, rectIn } from './tool.js';

/* ---- GWidget.vmData layout (indices into the script-side struct) ---- */
export const GW_VM_SYS_TYPE = 0;
export const GW_VM_TYPE = 1;
export const GW_VM_VERAION = 2;
export const GW_VM_X = 3;
export const GW_VM_Y = 4;
export const GW_VM_W = 5;
export const GW_VM_H = 6;
export const GW_VM_XX = 7;
export const GW_VM_YY = 8;
export const GW_VM_BORDERLEFT = 9;
export const GW_VM_BORDERTOP = 10;
export const GW_VM_BORDERRIGHT = 11;
export const GW_VM_BORDERBOTTOM = 12;
export const GW_VM_MAX_WIDTH = 13;
export const GW_VM_MIN_WIDTH = 14;
export const GW_VM_MAX_HEIGHT = 15;
export const GW_VM_MIN_HEIGHT = 16;
export const GW_VM_Z_ORDER = 17;
export const GW_VM_FUNC_CYCLE = 18;
export const GW_VM_FUNC_CYCLEUI = 19;
export const GW_VM_FUNC_PAINT = 20;
export const GW_VM_FUNC_PACKET = 21;
export const GW_VM_FUNC_DESTROY = 22;
export const GW_VM_FUNC_SEND_EVENT = 23;
export const GW_VM_FUNC_GET_PERFECT_WIDTH = 24;
export const GW_VM_FUNC_GET_PERFECT_HEIGHT = 25;
export const GW_VM_FUNC_RESIZE = 26;
export const GW_VM_CAN_MOUSE_CLICKED = 27;
export const GW_VM_CAN_MOUSE_DRAGGED = 28;
export const GW_VM_JAVA_GWIDGET = 29;
export const GW_VM_ID = 30;
export const GW_VM_OFFSET_X = 31;
export const GW_VM_OFFSET_Y = 32;
export const GW_VM_SELF = 33;
export const GW_VM_FUNC_PAINT_BEFORE = 34;
export const GW_VM_FUNC_PAINT_AFTER = 35;

/* events */
export const GW_EVENT_GET_FOCUS = 0;
export const GW_EVENT_LOST_FOCUS = 1;
export const GW_EVENT_MOUSE_CLICKED = 2;

/* paint types in a window's PAINT call stack */
export const PAINT_TYPE_IN_VM = 0;
export const PAINT_TYPE_IN_JAVA = 1;
export const PAINT_TYPE_AFTER = 2;
export const PAINT_TYPE_GIPAINT = 3;

let gwidgetKey = 0;

/** GWidget */
export class GWidget {
  constructor(vmGame, self, vmData, name) {
    this.vmData = vmData;
    this.name = name;
    this.vmGame = vmGame;

    if (vmData[GW_VM_MAX_WIDTH] === 0) vmData[GW_VM_MAX_WIDTH] = 0x7fffffff;
    if (vmData[GW_VM_MAX_HEIGHT] === 0) vmData[GW_VM_MAX_HEIGHT] = 0x7fffffff;

    vmData[GW_VM_JAVA_GWIDGET] = ++gwidgetKey;
    vmGame.putGWidget(this);

    vmData[GW_VM_TYPE] = vmData[GW_VM_SYS_TYPE];
    vmData[GW_VM_SELF] = vmGame.gtvm.getRealizeAdrr(vmGame.gtvm.makeTempObject(vmData));
  }

  /* plain fields the Java class declares */
  parent = null;
  isScale = true;
  noNeedLayout = false;
  isFocus = false;
  enableFocus = true;
  isOutViewFlag = false;
  realHeight = 0;
  outHeight = 0;
  grid3Data = null;
  pressX = 0;
  pressY = 0;
  isShow = true;
  borderLayoutType = 0;
  font = null; // falls back to the platform default when unset

  getVMDataCopy() {
    return Int32Array.from(this.vmData);
  }

  setCloneData(gWidget) {
    gWidget.enableFocus = this.enableFocus;
    gWidget.parent = this.parent;
    gWidget.name = this.name;
    gWidget.noNeedLayout = this.noNeedLayout;
    gWidget.isOutViewFlag = this.isOutViewFlag;
    gWidget.isFocus = this.isFocus;
    gWidget.isScale = this.isScale;
    gWidget.pressX = this.pressX;
    gWidget.pressY = this.pressY;
    gWidget.isShow = this.isShow;
    gWidget.borderLayoutType = this.borderLayoutType;
    if (this.grid3Data != null) gWidget.grid3Data = Int32Array.from(this.grid3Data);
    gWidget.realHeight = this.realHeight;
    gWidget.outHeight = this.outHeight;
  }

  getClone(vmGame) {
    const gWidget = new GWidget(vmGame, 0, this.getVMDataCopy(), '');
    this.setCloneData(gWidget);
    return gWidget;
  }

  static getCloneArray(vmGame, gWidget, count) {
    const out = new Array(count);
    for (let i = 0; i < count; i++) out[i] = gWidget.getClone(vmGame).vmData;
    return out;
  }

  setFont(font) { this.font = font; }
  getFont() { return this.font ?? this.vmGame.defaultFont; }

  getShow() { return this.isShow; }

  setShow(isShow) {
    this.isShow = isShow;
    this.reCreateStack();
  }

  SetNeedLayout(needLayout) {
    this.noNeedLayout = !needLayout;
  }

  setPressXY(x, y) { this.pressX = x; this.pressY = y; }
  getPressX() { return this.pressX; }
  getPressY() { return this.pressY; }

  move(offsetX, offsetY) {
    this.vmData[GW_VM_X] += offsetX;
    this.vmData[GW_VM_Y] += offsetY;
    this.vmData[GW_VM_XX] += offsetX;
    this.vmData[GW_VM_YY] += offsetY;
  }

  getParentWindow() {
    if (this.parent instanceof GWindow) return this.parent;
    return this.parent != null ? this.parent.getParentWindow() : null;
  }

  setGrid3Data(gridX, gridY, gridHCount, gridVCount, borderTop, borderBottom, borderLeft, borderRight) {
    this.grid3Data = Int32Array.of(gridX, gridY, gridHCount, gridVCount, borderTop, borderBottom, borderLeft, borderRight);
  }

  isFocused() { return this.isFocus; }
  setScale(isScale) { this.isScale = isScale; }
  setEnableFocus(enableFocus) { this.enableFocus = enableFocus; }

  setBounds(_x, _y, _w, _h) {
    this.setPos(_x, _y);
    // ScreenCanReset == false build: clamp to the game view width
    if (_x + _w > this.vmGame.viewWidth) {
      _w = this.vmGame.viewWidth - _x;
    }
    this.vmData[GW_VM_W] = _w;
    this.vmData[GW_VM_H] = _h;

    if (this.isScale && this.vmData[GW_VM_FUNC_RESIZE] !== 0) {
      this.vmGame.gtvm.callback(this.vmData[GW_VM_FUNC_RESIZE],
        [this.vmData[GW_VM_SELF], _x, _y, _w, _h]);
    }
  }

  setPos(x, y) {
    this.vmData[GW_VM_X] = x;
    this.vmData[GW_VM_Y] = y;
    this.vmData[GW_VM_XX] = this.getAbsX();
    this.vmData[GW_VM_YY] = this.getAbsY();
  }

  setSize(w, h) {
    this.vmData[GW_VM_W] = w;
    this.vmData[GW_VM_H] = h;
  }

  setMinSize(w, h) {
    this.vmData[GW_VM_MIN_WIDTH] = w;
    this.vmData[GW_VM_MIN_HEIGHT] = h;
  }

  setBorder(borderTop, borderBottom, borderLeft, borderRight) {
    this.vmData[GW_VM_BORDERTOP] = borderTop;
    this.vmData[GW_VM_BORDERLEFT] = borderLeft;
    this.vmData[GW_VM_BORDERRIGHT] = borderRight;
    this.vmData[GW_VM_BORDERBOTTOM] = borderBottom;
  }

  getAbsX() {
    let x = this.vmData[GW_VM_X];
    let p = this.parent;
    while (p != null) {
      x += p.vmData[GW_VM_X];
      p = p.parent;
    }
    return x;
  }

  getAbsY() {
    let y = this.vmData[GW_VM_Y];
    let p = this.parent;
    while (p != null) {
      y += p.vmData[GW_VM_Y];
      p = p.parent;
    }
    return y;
  }

  setOutView(isOutView) {
    this.isOutViewFlag = isOutView;
    this.reCreateStack();
  }

  reCreateStack() {
    if (this instanceof GWindow) {
      this.reCreateStackFlag = true;
    } else {
      const win = this.getParentWindow();
      if (win != null) win.reCreateStackFlag = true;
    }
  }

  isOutView() { return this.isOutViewFlag; }

  getX() { return this.vmData[GW_VM_X]; }
  getY() { return this.vmData[GW_VM_Y]; }
  getW() { return this.vmData[GW_VM_W]; }
  getMinW() { return this.vmData[GW_VM_MIN_WIDTH]; }
  getMaxW() { return this.vmData[GW_VM_MAX_WIDTH]; }

  freeVMObj() {
    const gtvm = this.vmGame.gtvm;
    if (gtvm.followPointer(this.vmData[GW_VM_SELF]) != null) {
      gtvm.free(this.vmData[GW_VM_SELF]);
    }
  }

  parentNeedScroll() {
    let p = this.parent;
    while (p != null) {
      if (p.needScrollBar) return true;
      p = p.parent;
    }
    return false;
  }

  getIntersect() {
    let ret;
    if (this.parent != null) {
      const pr = this.parent.getIntersect();
      ret = intersectRect(
        this.vmData[GW_VM_XX] + this.vmData[GW_VM_OFFSET_X],
        this.vmData[GW_VM_YY] + this.vmData[GW_VM_OFFSET_Y],
        this.vmData[GW_VM_W], this.vmData[GW_VM_H],
        pr[0], pr[1], pr[2], pr[3]);
    } else {
      ret = [
        this.vmData[GW_VM_XX] + this.vmData[GW_VM_OFFSET_X],
        this.vmData[GW_VM_YY] + this.vmData[GW_VM_OFFSET_Y],
        this.vmData[GW_VM_W], this.vmData[GW_VM_H],
      ];
    }
    return ret;
  }
}

function intersectRect(x1, y1, w1, h1, x2, y2, w2, h2) {
  const nx = Math.max(x1, x2);
  const ny = Math.max(y1, y2);
  const nx2 = Math.min(x1 + w1, x2 + w2);
  const ny2 = Math.min(y1 + h1, y2 + h2);
  return [nx, ny, Math.max(0, nx2 - nx), Math.max(0, ny2 - ny)];
}

/* ---- GContainer layout constants ---- */
export const GW_LAYOUT_TYPE_NONE = 0x00;
export const GW_LAYOUT_TYPE_H = 0x01;
export const GW_LAYOUT_TYPE_V = 0x02;
export const GW_LAYOUT_TYPE_GRID = 0x04;
export const GW_LAYOUT_TYPE_GRID2 = 0x08;
export const GW_LAYOUT_TYPE_GRID3 = 0x10;
export const GW_LAYOUT_TYPE_BORDER = 0x11;

export const GW_LAYOUT_ALIGN_NONE = 0x0000;
export const GW_LAYOUT_ALIGN_FILL = 0x0100;
export const GW_LAYOUT_ALIGN_HCENTER = 0x0200;
export const GW_LAYOUT_ALIGN_VCENTER = 0x0400;

export const GW_BORDER_LAYOUT_NORTH = 0;
export const GW_BORDER_LAYOUT_SOUTH = 1;
export const GW_BORDER_LAYOUT_WEST = 2;
export const GW_BORDER_LAYOUT_EAST = 3;
export const GW_BORDER_LAYOUT_CENTER = 4;

const L_MODE = 0;
const L_HGAP = 1;
const L_VGAP = 2;
const L_ALIGN = 3;
const L_ROWS = 4;
const L_COLS = 5;
const L_GRID_W = 4;
const L_GRID_H = 5;
const L_UP_GAP = 1;
const L_DOWN_GAP = 2;
const L_LEFT_GAP = 3;
const L_RIGHT_GAP = 4;

/** GContainer */
export class GContainer extends GWidget {
  constructor(vmGame, self, vmData, name) {
    super(vmGame, self, vmData, name);
    /** @type {GWidget[]} */
    this.children = [];
    this.layoutData = new Int32Array(6);
    this.needScrollBar = false;
    this.gsb = null;
    this.firstInViewIndex = -1;
    this.lastInViewIndex = -1;
    this.isJavaPaint = false;
    this.isIntersectView = false;
  }

  setCloneData(vmGame, gContainer) {
    super.setCloneData(gContainer);
    gContainer.layoutData.set(this.layoutData);
    gContainer.needScrollBar = this.needScrollBar;
    if (this.gsb != null) gContainer.gsb = this.gsb.getClone(vmGame);
    gContainer.firstInViewIndex = this.firstInViewIndex;
    gContainer.lastInViewIndex = this.lastInViewIndex;
    gContainer.isJavaPaint = this.isJavaPaint;
    gContainer.isIntersectView = this.isIntersectView;
    for (const child of this.children) gContainer.add(child.getClone(vmGame));
  }

  getClone(vmGame) {
    const c = new GContainer(vmGame, 0, this.getVMDataCopy(), null);
    this.setCloneData(vmGame, c);
    return c;
  }

  /* ------------------------------------------------------- children */

  getChildren() {
    return this.children.map((c) => c.vmData);
  }

  getJavaChildren() {
    return this.children.slice();
  }

  getChild(index) {
    return this.children[index].vmData;
  }

  getJavaChild(index) {
    return this.children[index];
  }

  batchAdd(gWidget, count) {
    for (let i = 0; i < count; i++) {
      const clone = gWidget.getClone(this.vmGame);
      this.add(clone);
      clone.vmData[GW_VM_ID] = i;
    }
  }

  toTop(index) {
    const w = this.children[index];
    this.children.splice(index, 1);
    this.add(w);
  }

  toBottom(index) {
    const w = this.children[index];
    this.children.splice(index, 1);
    this.insert(w, 0);
  }

  addScrollBar(gsb) {
    this.gsb = gsb;
    gsb.parent = this;
  }

  getScrollBar() {
    return this.gsb;
  }

  add(child, borderLayoutType) {
    this.children.push(child);
    this.initChild(child);
    if (borderLayoutType !== undefined) child.borderLayoutType = borderLayoutType;
  }

  insert(child, index) {
    this.children.splice(index, 0, child);
    this.initChild(child);
  }

  getIndex(gWidget) {
    return this.children.indexOf(gWidget);
  }

  initChild(child) {
    child.parent = this;
    child.vmData[GW_VM_Z_ORDER] = this.vmData[GW_VM_Z_ORDER] + 1;
    this.reCreateStack();
  }

  move(offsetX, offsetY) {
    super.move(offsetX, offsetY);
    this.setAbs();
  }

  remove(child) {
    const i = this.children.indexOf(child);
    if (i >= 0) this.children.splice(i, 1);

    if (child instanceof GContainer) {
      child.destroy();
    } else {
      if (child.vmData[GW_VM_FUNC_DESTROY] > 0) {
        this.vmGame.gtvm.callback(child.vmData[GW_VM_FUNC_DESTROY], [child.vmData[GW_VM_SELF]]);
      }
      const win = child.getParentWindow();
      if (win != null && win.focusWidget === child) win.focusWidget = null;
    }

    this.reCreateStack();
    child.freeVMObj();
    this.vmGame.removeGWidget(child);
  }

  clear() {
    this.destroyChild(this.getParentWindow());
    if (this.gsb != null) this.gsb.reset();
    this.reCreateStack();
  }

  destroy() {
    this.destroyChild(this.getParentWindow());
    if (this.gsb != null) this.gsb.reset();
  }

  destroyChild(parentWindow) {
    for (const child of this.children) {
      if (child instanceof GContainer) {
        child.destroyChild(parentWindow ?? (child instanceof GWindow ? child : child.getParentWindow()));
      } else {
        if (child.vmData[GW_VM_FUNC_DESTROY] > 0) {
          this.vmGame.gtvm.callback(child.vmData[GW_VM_FUNC_DESTROY], [child.vmData[GW_VM_SELF]]);
        }
        if (parentWindow != null && parentWindow.focusWidget === child) {
          parentWindow.focusWidget = null;
        }
      }
      child.freeVMObj();
      this.vmGame.removeGWidget(child);
    }
    this.children.length = 0;
  }

  /* -------------------------------------------------------- scrolling */

  setSrollBar(oldFocusWidget, newFocusWidget) {
    if (oldFocusWidget === newFocusWidget) return;

    let needScrollToo = false;
    if (this.isIntersectView) {
      if ((newFocusWidget.vmData[GW_VM_Y] < 0 && newFocusWidget.vmData[GW_VM_Y] + newFocusWidget.vmData[GW_VM_H] > 0) ||
          (newFocusWidget.vmData[GW_VM_Y] < this.vmData[GW_VM_H] && newFocusWidget.vmData[GW_VM_Y] + newFocusWidget.vmData[GW_VM_H] > this.vmData[GW_VM_H])) {
        needScrollToo = true;
      }
    }
    if (!newFocusWidget.isOutView() && !needScrollToo) return;

    let offsetY = 0;
    const firstWidget = this.children[this.firstInViewIndex];
    const lastWidget = this.children[this.lastInViewIndex];

    if (this.children[0] === newFocusWidget) {
      if (this.gsb != null) offsetY = this.gsb.scrollPos;
    } else if (newFocusWidget === this.children[this.children.length - 1]) {
      const h = lastWidget.vmData[GW_VM_Y] + lastWidget.vmData[GW_VM_H] - firstWidget.vmData[GW_VM_Y];
      offsetY = h - newFocusWidget.vmData[GW_VM_Y] - newFocusWidget.vmData[GW_VM_H] +
        firstWidget.vmData[GW_VM_Y];
    } else if (newFocusWidget.vmData[GW_VM_Y] < oldFocusWidget.vmData[GW_VM_Y]) {
      if (oldFocusWidget.isOutView()) {
        offsetY = -newFocusWidget.vmData[GW_VM_Y] + firstWidget.vmData[GW_VM_Y] + firstWidget.vmData[GW_VM_H];
      } else if (needScrollToo) {
        offsetY = -newFocusWidget.vmData[GW_VM_Y];
      } else {
        offsetY = -newFocusWidget.vmData[GW_VM_Y] + firstWidget.vmData[GW_VM_Y];
      }
    } else {
      if (oldFocusWidget.isOutView()) {
        offsetY = -oldFocusWidget.vmData[GW_VM_Y] + firstWidget.vmData[GW_VM_Y];
      } else if (needScrollToo) {
        offsetY = -newFocusWidget.vmData[GW_VM_Y] - newFocusWidget.vmData[GW_VM_H] + this.vmData[GW_VM_H];
      } else {
        const h = lastWidget.vmData[GW_VM_Y] + lastWidget.vmData[GW_VM_H];
        offsetY = -newFocusWidget.vmData[GW_VM_Y] - newFocusWidget.vmData[GW_VM_H] + h;
      }
    }

    this.setChildrenOffset(0, offsetY);
  }

  moveUp() {
    if (this.lastInViewIndex > -1 && this.lastInViewIndex + 1 < this.children.length) {
      const a = this.children[this.lastInViewIndex];
      const b = this.children[this.lastInViewIndex + 1];
      this.setChildrenOffset(0, a.vmData[GW_VM_Y] - b.vmData[GW_VM_Y]);
    }
  }

  moveDown() {
    if (this.firstInViewIndex > -1 && this.firstInViewIndex > 0) {
      const a = this.children[this.firstInViewIndex - 1];
      const b = this.children[this.firstInViewIndex];
      this.setChildrenOffset(0, b.vmData[GW_VM_Y] - a.vmData[GW_VM_Y]);
    }
  }

  moveUpPage() {}
  moveDownPage() {}

  resetScrollBar() {
    let parentWin = this.getParentWindow();
    if (parentWin == null && this instanceof GWindow) parentWin = this;
    if (parentWin.focusWidget == null) return;

    for (const child of this.children) {
      if (child instanceof GContainer) {
        if (child.children.includes(parentWin.focusWidget)) {
          child.setScrollbar(parentWin.focusWidget);
        } else if (child.children.length > 0) {
          child.setScrollbar(child.children[0]);
        }
        child.resetScrollBar();
      }
    }
  }

  setScrollbar(focusGWidget) {
    if (this.gsb != null && this.gsb.maxScrollDis > 0) {
      this.gsb.scrollPos = 0;
      this.setSrollBar(this.children[0], focusGWidget);
    }
  }

  setChildrenOffset(offsetX, offsetY) {
    this.firstInViewIndex = -1;
    this.lastInViewIndex = -1;

    if (this.gsb != null) {
      if (this.gsb.scrollPos - offsetY > this.gsb.maxScrollDis) {
        offsetY = this.gsb.scrollPos - this.gsb.maxScrollDis;
        this.gsb.scrollPos = this.gsb.maxScrollDis;
      } else {
        const oldScrollPos = this.gsb.scrollPos;
        this.gsb.scrollPos -= offsetY;
        if (this.gsb.scrollPos < 0) {
          this.gsb.scrollPos = 0;
          offsetY = oldScrollPos;
        }
      }
    }

    const count = this.children.length;
    for (let i = 0; i < count; i++) {
      const gWidget = this.children[i];
      gWidget.setPos(gWidget.vmData[GW_VM_X] + offsetX, gWidget.vmData[GW_VM_Y] + offsetY);

      const inView = this.isIntersectView
        ? rectIntersect(0, 0, this.vmData[GW_VM_W], this.vmData[GW_VM_H],
            gWidget.vmData[GW_VM_X], gWidget.vmData[GW_VM_Y], gWidget.vmData[GW_VM_W], gWidget.vmData[GW_VM_H])
        : rectContain(0, 0, this.vmData[GW_VM_W], this.vmData[GW_VM_H],
            gWidget.vmData[GW_VM_X], gWidget.vmData[GW_VM_Y], gWidget.vmData[GW_VM_W], gWidget.vmData[GW_VM_H]);

      if (inView) {
        gWidget.setOutView(false);
        if (this.firstInViewIndex === -1) this.firstInViewIndex = i;
        if (i === count - 1 && this.lastInViewIndex === -1) this.lastInViewIndex = i - 1;
      } else {
        gWidget.setOutView(true);
        if (this.lastInViewIndex === -1 && this.firstInViewIndex !== -1) this.lastInViewIndex = i - 1;
      }
    }

    this.setAbs();
  }

  /* ------------------------------------------------------ bounds */

  setBounds(_x, _y, _w, _h) {
    super.setBounds(_x, _y, _w, _h);
    this.setAbs();
    this._placeScrollBar();
  }

  setPos(x, y) {
    super.setPos(x, y);
    this.setAbs();
    if (this.gsb != null) {
      switch (this.gsb.align) {
        case GScrollBar.GSB_ALIGN_CENTER:
          this.gsb.setPos((this.vmData[GW_VM_W] - this.gsb.vmData[GW_VM_MIN_WIDTH]) / 2 | 0, 0);
          break;
        case GScrollBar.GSB_ALIGN_RIGHT:
          this.gsb.setPos(this.vmData[GW_VM_W] - this.gsb.vmData[GW_VM_MIN_WIDTH], 0);
          break;
      }
    }
  }

  _placeScrollBar() {
    if (this.gsb != null) {
      switch (this.gsb.align) {
        case GScrollBar.GSB_ALIGN_CENTER:
          this.gsb.setBounds((this.vmData[GW_VM_W] - this.gsb.vmData[GW_VM_MIN_WIDTH]) / 2 | 0, 0,
            this.gsb.vmData[GW_VM_MIN_WIDTH], this.vmData[GW_VM_H]);
          break;
        case GScrollBar.GSB_ALIGN_RIGHT:
          this.gsb.setBounds(this.vmData[GW_VM_W] - this.gsb.vmData[GW_VM_MIN_WIDTH], 0,
            this.gsb.vmData[GW_VM_MIN_WIDTH], this.vmData[GW_VM_H]);
          break;
      }
    }
  }

  setAbs() {
    for (const gWidget of this.children) {
      gWidget.vmData[GW_VM_XX] = gWidget.getAbsX();
      gWidget.vmData[GW_VM_YY] = gWidget.getAbsY();
      if (gWidget instanceof GContainer) {
        gWidget.setAbs();
        if (gWidget.gsb != null) {
          gWidget.gsb.vmData[GW_VM_XX] = gWidget.gsb.getAbsX();
          gWidget.gsb.vmData[GW_VM_YY] = gWidget.gsb.getAbsY();
        }
      }
    }
  }

  setIsJavaPaint(isJavaPaint) {
    this.isJavaPaint = isJavaPaint;
  }

  /* --------------------------------------------------- hit testing */

  /** GContainer.serchWdiget — pointer search inside a known top window. */
  searchWidget(x, y) {
    return this._searchWidget(x, y);
  }

  /** GContainer.serchWdiget2 — same walk, used from the global top window. */
  searchWidget2(x, y) {
    return this._searchWidget(x, y);
  }

  _searchWidget(x, y) {
    // topmost child first; containers are descended before their own hit box
    for (let i = this.children.length - 1; i >= 0; i--) {
      const w = this.children[i];
      if (!w.isShow || w.isOutView()) continue;
      if (w instanceof GContainer) {
        const found = w._searchWidget(x, y);
        if (found != null) return found;
      }
      if ((w.vmData[GW_VM_CAN_MOUSE_CLICKED] === 1 || w.vmData[GW_VM_CAN_MOUSE_DRAGGED] === 1) &&
          rectIn(w.vmData[GW_VM_XX] + w.vmData[GW_VM_OFFSET_X],
                 w.vmData[GW_VM_YY] + w.vmData[GW_VM_OFFSET_Y],
                 w.vmData[GW_VM_W], w.vmData[GW_VM_H], x, y)) {
        return w;
      }
    }
    if (this.needScrollBar && this.gsb != null) {
      const sb = this.gsb;
      if ((sb.vmData[GW_VM_CAN_MOUSE_CLICKED] === 1 || sb.vmData[GW_VM_CAN_MOUSE_DRAGGED] === 1) &&
          rectIn(sb.vmData[GW_VM_XX], sb.vmData[GW_VM_YY], sb.vmData[GW_VM_W], sb.vmData[GW_VM_H], x, y)) {
        return sb;
      }
    }
    return null;
  }

  /* ------------------------------------------------------- layout */

  setLayoutMode(layoutMode, data1, data2, data3, data4, data5) {
    this.layoutData[L_MODE] = layoutMode;
    this.layoutData[1] = data1;
    this.layoutData[2] = data2;
    this.layoutData[3] = data3;
    this.layoutData[4] = data4;
    this.layoutData[5] = data5;
  }

  setHLayout(hgap, align) {
    this.layoutData[L_MODE] = GW_LAYOUT_TYPE_H;
    this.layoutData[L_HGAP] = hgap;
    this.layoutData[L_ALIGN] = align;
  }

  setVLayout(vgap, align) {
    this.layoutData[L_MODE] = GW_LAYOUT_TYPE_V;
    this.layoutData[L_VGAP] = vgap;
    this.layoutData[L_ALIGN] = align;
  }

  setGridLayout(rows, cols) {
    this.layoutData[L_MODE] = GW_LAYOUT_TYPE_GRID;
    this.layoutData[L_ROWS] = rows;
    this.layoutData[L_COLS] = cols;
  }

  setGrid2Layout(hgap, vgap, gridW, gridH) {
    this.layoutData[L_MODE] = GW_LAYOUT_TYPE_GRID2;
    this.layoutData[L_HGAP] = hgap;
    this.layoutData[L_VGAP] = vgap;
    this.layoutData[L_GRID_W] = gridW;
    this.layoutData[L_GRID_H] = gridH;
    this.layoutData[L_ALIGN] = GW_LAYOUT_ALIGN_FILL;
  }

  setGrid3Layout(rows, cols) {
    this.layoutData[L_MODE] = GW_LAYOUT_TYPE_GRID3;
    this.layoutData[L_ROWS] = rows;
    this.layoutData[L_COLS] = cols;
  }

  setBorderLayout(upGap, downGap, leftGap, rightGap) {
    this.layoutData[L_MODE] = GW_LAYOUT_TYPE_BORDER;
    this.layoutData[L_UP_GAP] = upGap;
    this.layoutData[L_DOWN_GAP] = downGap;
    this.layoutData[L_LEFT_GAP] = leftGap;
    this.layoutData[L_RIGHT_GAP] = rightGap;
  }

  layout() {
    this.layout2();
    this.align();
  }

  layout2() {
    switch (this.layoutData[L_MODE]) {
      case GW_LAYOUT_TYPE_NONE:
        for (const w of this.children) {
          if (w.noNeedLayout) continue;
          if (w instanceof GContainer) w.layout2();
        }
        break;
      case GW_LAYOUT_TYPE_H: this.layoutH(); break;
      case GW_LAYOUT_TYPE_V: this.layoutV(); break;
      case GW_LAYOUT_TYPE_GRID: this.layoutG(); break;
      case GW_LAYOUT_TYPE_GRID2: this.layoutG2(); break;
      case GW_LAYOUT_TYPE_BORDER: this.borderLayout(); break;
    }
  }

  getPerfectWidth(gWidget, layoutW) {
    let perfectWidth = 0;
    if (gWidget.vmData[GW_VM_FUNC_GET_PERFECT_WIDTH] !== 0) {
      perfectWidth = this.vmGame.gtvm.callback(gWidget.vmData[GW_VM_FUNC_GET_PERFECT_WIDTH],
        [gWidget.vmData[GW_VM_SELF], gWidget.vmData[GW_VM_MAX_WIDTH]]);
    } else {
      perfectWidth = gWidget.vmData[GW_VM_MIN_WIDTH] + gWidget.vmData[GW_VM_BORDERLEFT] +
        gWidget.vmData[GW_VM_BORDERRIGHT];
    }
    if (perfectWidth > layoutW && layoutW > 0) perfectWidth = layoutW;
    return perfectWidth;
  }

  getPerfectHeight(gWidget, width, layoutHeight) {
    let perfectHeight = 0;
    if (gWidget.vmData[GW_VM_FUNC_GET_PERFECT_HEIGHT] !== 0) {
      perfectHeight = this.vmGame.gtvm.callback(gWidget.vmData[GW_VM_FUNC_GET_PERFECT_HEIGHT],
        [gWidget.vmData[GW_VM_SELF], width, gWidget.vmData[GW_VM_MAX_HEIGHT]]);
    } else {
      perfectHeight = gWidget.vmData[GW_VM_MIN_HEIGHT] + gWidget.vmData[GW_VM_BORDERTOP] +
        gWidget.vmData[GW_VM_BORDERBOTTOM];
    }
    if (perfectHeight > layoutHeight && layoutHeight > 0) perfectHeight = layoutHeight;
    return perfectHeight;
  }

  layoutH() {
    const nextXStart = this.vmData[GW_VM_BORDERLEFT];
    let nextX = nextXStart;
    const nextY = this.vmData[GW_VM_BORDERTOP];
    let maxH = 0;
    for (const w of this.children) {
      if (w.noNeedLayout) continue;
      if (w instanceof GContainer) w.layout2();

      const pw = this.getPerfectWidth(w, 0);
      const ph = this.getPerfectHeight(w, pw, 0);
      w.setBounds(nextX, nextY, pw, ph);
      nextX += pw + this.layoutData[L_HGAP];
      maxH = Math.max(maxH, ph);
    }

    if (this.isScale) {
      nextX -= this.layoutData[L_HGAP];
      this.setSize(nextX + this.vmData[GW_VM_BORDERRIGHT],
        maxH + this.vmData[GW_VM_BORDERTOP] + this.vmData[GW_VM_BORDERBOTTOM]);
      this.setMinSize(nextX - nextXStart, maxH - this.vmData[GW_VM_BORDERTOP]);
    }
  }

  layoutV() {
    const nextXStart = this.vmData[GW_VM_BORDERLEFT];
    const nextYStart = this.vmData[GW_VM_BORDERTOP];
    let nextX = nextXStart;
    let nextY = nextYStart;
    let maxW = 0;
    let stopNextY = 0;
    this.firstInViewIndex = -1;
    this.lastInViewIndex = -1;
    let isOutOfContainer = false;

    for (let i = 0; i < this.children.length; i++) {
      const w = this.children[i];
      w.setOutView(false);
      if (w.noNeedLayout) continue;
      if (w instanceof GContainer) w.layout2();

      const pw = this.getPerfectWidth(w, 0);
      const ph = this.getPerfectHeight(w, pw, 0);
      w.setBounds(nextX, nextY, pw, ph);

      if (stopNextY === 0) {
        if (!this.isIntersectView) {
          if (nextY + ph + this.vmData[GW_VM_BORDERBOTTOM] > this.vmData[GW_VM_MAX_HEIGHT]) {
            isOutOfContainer = true;
          }
        } else if (nextY + w.vmData[GW_VM_MIN_HEIGHT] > this.vmData[GW_VM_MAX_HEIGHT]) {
          isOutOfContainer = true;
        }
      }

      if (isOutOfContainer) w.setOutView(true);

      if (isOutOfContainer && stopNextY === 0) {
        if (this.firstInViewIndex === -1) this.firstInViewIndex = 0;
        if (this.lastInViewIndex === -1) this.lastInViewIndex = i - 1;
        stopNextY = nextY;
      }

      nextY += ph + this.layoutData[L_VGAP];
      maxW = Math.max(maxW, pw);
    }

    if (this.firstInViewIndex === -1) this.firstInViewIndex = 0;
    if (this.lastInViewIndex === -1) this.lastInViewIndex = this.children.length - 1;
    this.realHeight = nextY - this.layoutData[L_VGAP] + this.vmData[GW_VM_BORDERBOTTOM];
    if (stopNextY > 0) {
      this.needScrollBar = true;
      this.outHeight = nextY - stopNextY;
      if (this.gsb != null) this.gsb.maxScrollDis = this.outHeight;
      nextY = stopNextY;
    } else {
      this.needScrollBar = false;
    }
    if (this.isScale) {
      nextY -= this.layoutData[L_VGAP];
      let width = maxW + this.vmData[GW_VM_BORDERLEFT] + this.vmData[GW_VM_BORDERRIGHT];
      if (width < this.vmData[GW_VM_MIN_WIDTH]) width = this.vmData[GW_VM_MIN_WIDTH];
      else if (width > this.vmData[GW_VM_MAX_WIDTH]) width = this.vmData[GW_VM_MAX_WIDTH];
      if (nextY + this.vmData[GW_VM_BORDERBOTTOM] > this.vmData[GW_VM_MAX_HEIGHT]) {
        nextY = this.vmData[GW_VM_MAX_HEIGHT] - this.vmData[GW_VM_BORDERBOTTOM];
      }
      this.setSize(width, nextY + this.vmData[GW_VM_BORDERBOTTOM]);
      this.setMinSize(maxW - this.vmData[GW_VM_BORDERLEFT], nextY - this.vmData[GW_VM_BORDERRIGHT]);
    }
  }

  align() {
    const D = this.vmData;
    switch (this.layoutData[L_ALIGN]) {
      case GW_LAYOUT_ALIGN_NONE:
        for (const w of this.children) {
          if (w.noNeedLayout) continue;
          if (w instanceof GContainer) w.align();
        }
        break;
      case GW_LAYOUT_ALIGN_VCENTER:
        for (const w of this.children) {
          if (w.noNeedLayout) continue;
          w.setPos(w.vmData[GW_VM_X],
            w.vmData[GW_VM_Y] + ((D[GW_VM_H] - D[GW_VM_BORDERTOP] - D[GW_VM_BORDERBOTTOM] - w.vmData[GW_VM_H]) / 2) | 0);
          if (w instanceof GContainer) w.align();
        }
        break;
      case GW_LAYOUT_ALIGN_HCENTER:
        for (const w of this.children) {
          if (w.noNeedLayout) continue;
          w.setPos(w.vmData[GW_VM_X] +
            ((D[GW_VM_W] - D[GW_VM_BORDERLEFT] - D[GW_VM_BORDERRIGHT] - w.vmData[GW_VM_W]) / 2) | 0,
            w.vmData[GW_VM_Y]);
          if (w instanceof GContainer) w.align();
        }
        break;
      case GW_LAYOUT_ALIGN_FILL: {
        const count = this.children.length;
        let perWidth = 0;
        if (this.layoutData[L_MODE] === GW_LAYOUT_TYPE_H && count > 0) {
          perWidth = ((D[GW_VM_W] - D[GW_VM_BORDERLEFT] - D[GW_VM_BORDERRIGHT]) -
            (count - 1) * this.layoutData[L_HGAP]) / count | 0;
        }
        for (let i = 0; i < count; i++) {
          const w = this.children[i];
          if (w.noNeedLayout) continue;
          if (this.layoutData[L_MODE] === GW_LAYOUT_TYPE_V) {
            w.setBounds(w.vmData[GW_VM_X], w.vmData[GW_VM_Y],
              D[GW_VM_W] - D[GW_VM_BORDERLEFT] - D[GW_VM_BORDERRIGHT], w.vmData[GW_VM_H]);
          } else if (this.layoutData[L_MODE] === GW_LAYOUT_TYPE_H) {
            let perX = D[GW_VM_BORDERLEFT] + (perWidth + this.layoutData[L_HGAP]) * i +
              ((perWidth - w.vmData[GW_VM_W]) / 2) | 0;
            let realWidth = w.vmData[GW_VM_W];
            if (this.isScale) {
              perX = D[GW_VM_BORDERLEFT] + (perWidth + this.layoutData[L_HGAP]) * i;
              realWidth = perWidth;
            }
            w.setBounds(perX, w.vmData[GW_VM_Y], realWidth, w.vmData[GW_VM_H]);
          }
          if (w instanceof GContainer) w.align();
        }
        break;
      }
    }

    if (this.gsb != null) {
      switch (this.gsb.align) {
        case GScrollBar.GSB_ALIGN_CENTER:
          this.gsb.setBounds(((D[GW_VM_W] - this.gsb.vmData[GW_VM_MIN_WIDTH]) / 2) | 0, 0,
            this.gsb.vmData[GW_VM_MIN_WIDTH], D[GW_VM_H]);
          break;
        case GScrollBar.GSB_ALIGN_RIGHT:
          this.gsb.setBounds(D[GW_VM_W] - this.gsb.vmData[GW_VM_MIN_WIDTH], 0,
            this.gsb.vmData[GW_VM_MIN_WIDTH], D[GW_VM_H]);
          break;
      }
    }
  }

  layoutG() {
    if (this.layoutData[L_ROWS] > 0 && this.layoutData[L_COLS] > 0) {
      const D = this.vmData;
      const layoutX = D[GW_VM_BORDERLEFT];
      const layoutY = D[GW_VM_BORDERTOP];
      const layoutW = D[GW_VM_W] - D[GW_VM_BORDERLEFT] - D[GW_VM_BORDERRIGHT];
      const layoutH = D[GW_VM_H] - D[GW_VM_BORDERTOP] - D[GW_VM_BORDERBOTTOM];
      const gridW = ((layoutW + this.layoutData[L_HGAP]) / this.layoutData[L_COLS] - this.layoutData[L_HGAP]) | 0;
      const gridH = ((layoutH + this.layoutData[L_VGAP]) / this.layoutData[L_ROWS] - this.layoutData[L_VGAP]) | 0;
      this.layoutGrid(layoutX, layoutY, layoutW, layoutH, gridW, gridH,
        this.layoutData[L_ROWS], this.layoutData[L_COLS]);
    }
  }

  layoutG2() {
    const D = this.vmData;
    const layoutX = D[GW_VM_BORDERLEFT];
    const layoutY = D[GW_VM_BORDERTOP];
    const layoutW = D[GW_VM_W] - D[GW_VM_BORDERLEFT] - D[GW_VM_BORDERRIGHT];
    const layoutH = D[GW_VM_H] - D[GW_VM_BORDERTOP] - D[GW_VM_BORDERBOTTOM];
    const cols = ((layoutW + this.layoutData[L_HGAP]) / (this.layoutData[L_GRID_W] + this.layoutData[L_HGAP])) | 0;
    const rows = ((layoutH + this.layoutData[L_VGAP]) / (this.layoutData[L_GRID_H] + this.layoutData[L_VGAP])) | 0;
    this.layoutGrid(layoutX, layoutY, layoutW, layoutH,
      this.layoutData[L_GRID_W], this.layoutData[L_GRID_H], rows, cols);
  }

  layoutGrid(layoutX, layoutY, layoutW, layoutH, gridW, gridH, rows, cols) {
    const hRightGap = layoutW - (gridW + this.layoutData[L_HGAP]) * cols;
    layoutX += (hRightGap / 2) | 0;
    layoutW -= hRightGap;
    const vBottomGap = layoutH - (gridH + this.layoutData[L_VGAP]) * rows;
    layoutY += (vBottomGap / 2) | 0;
    layoutH -= vBottomGap;

    const count = this.children.length;
    let realRows = (count / cols) | 0;
    if (count % cols > 0) realRows++;
    for (let i = 0; i < realRows; i++) {
      for (let j = 0; j < cols; j++) {
        const idx = i * cols + j;
        if (idx > count - 1) break;
        const startX = layoutX + j * (gridW + this.layoutData[L_HGAP]) + (this.layoutData[L_HGAP] / 2) | 0;
        const startY = layoutY + i * (gridH + this.layoutData[L_VGAP]) + (this.layoutData[L_VGAP] / 2) | 0;
        const w = this.children[idx];
        if (w.isScale) {
          w.setBounds(startX, startY, gridW, gridH);
        } else {
          w.setBounds(startX + ((gridW - w.vmData[GW_VM_W]) / 2) | 0,
            startY + ((gridH - w.vmData[GW_VM_H]) / 2) | 0,
            w.vmData[GW_VM_W], w.vmData[GW_VM_H]);
        }
      }
    }

    const sum = cols * rows;
    if (sum < count) {
      this.needScrollBar = true;
      this.outHeight = (realRows - rows) * (gridH + this.layoutData[L_VGAP]);
      if (this.gsb != null) this.gsb.maxScrollDis = this.outHeight;
      this.firstInViewIndex = 0;
      this.lastInViewIndex = sum - 1;
      for (let i = sum; i < count; i++) this.children[i].setOutView(true);
    } else {
      this.needScrollBar = false;
      if (this.gsb != null) this.gsb.maxScrollDis = 0;
      this.firstInViewIndex = 0;
      this.lastInViewIndex = count - 1;
      for (let i = 0; i < count; i++) this.children[i].setOutView(false);
    }
  }

  grid3Layout() {
    const D = this.vmData;
    const layoutX = D[GW_VM_BORDERLEFT];
    const layoutY = D[GW_VM_BORDERTOP];
    const layoutW = D[GW_VM_W] - D[GW_VM_BORDERLEFT] - D[GW_VM_BORDERRIGHT];
    const layoutH = D[GW_VM_H] - D[GW_VM_BORDERTOP] - D[GW_VM_BORDERBOTTOM];

    const gridW = (layoutW / this.layoutData[L_COLS]) | 0;
    const gridH = (layoutH / this.layoutData[L_ROWS]) | 0;
    const realW = gridW * this.layoutData[L_COLS];
    const fixW = (layoutW % gridW) + 1;

    for (const w of this.children) {
      if (w.grid3Data != null) {
        const g = w.grid3Data;
        let gw = g[2] * gridW - g[6] - g[7];
        let gh = g[3] * gridH - g[4] - g[5];
        const gx = layoutX + g[0] * gridW + g[6];
        const gy = layoutY + g[1] * gridH + g[4];
        if ((g[0] + g[2]) * gridW === realW) gw += fixW;
        if (w.isScale) {
          w.setBounds(gx, gy, gw, gh);
        } else {
          w.setBounds(gx + ((gw - w.vmData[GW_VM_W]) / 2) | 0, gy + ((gh - w.vmData[GW_VM_H]) / 2) | 0,
            w.vmData[GW_VM_W], w.vmData[GW_VM_H]);
        }
      }
    }
  }

  getBorderLayoutGWidget(type) {
    return this.children.find((w) => w.borderLayoutType === type) ?? null;
  }

  borderLayout() {
    const D = this.vmData;
    let layoutX = D[GW_VM_BORDERLEFT];
    let layoutY = D[GW_VM_BORDERTOP];
    let layoutW = D[GW_VM_W] - D[GW_VM_BORDERLEFT] - D[GW_VM_BORDERRIGHT];
    let layoutH = D[GW_VM_H] - D[GW_VM_BORDERTOP] - D[GW_VM_BORDERBOTTOM];

    const north = this.getBorderLayoutGWidget(GW_BORDER_LAYOUT_NORTH);
    const south = this.getBorderLayoutGWidget(GW_BORDER_LAYOUT_SOUTH);
    const west = this.getBorderLayoutGWidget(GW_BORDER_LAYOUT_WEST);
    const east = this.getBorderLayoutGWidget(GW_BORDER_LAYOUT_EAST);
    const center = this.getBorderLayoutGWidget(GW_BORDER_LAYOUT_CENTER);

    let perfectWidth = layoutW;
    let perfectHeight = layoutH;

    if (north != null) {
      north.vmData[GW_VM_W] = layoutW;
      if (north instanceof GContainer) north.layout2();
      perfectHeight = this.getPerfectHeight(north, layoutW, layoutH);
      north.setBounds(layoutX, layoutY, layoutW, perfectHeight);
      layoutY += north.vmData[GW_VM_H] + this.layoutData[L_UP_GAP];
      layoutH -= north.vmData[GW_VM_H] + this.layoutData[L_UP_GAP];
      perfectHeight = layoutH;
    }

    if (south != null) {
      south.vmData[GW_VM_W] = layoutW;
      if (south instanceof GContainer) south.layout2();
      perfectHeight = this.getPerfectHeight(south, layoutW, layoutH);
      south.setBounds(layoutX, D[GW_VM_H] - perfectHeight - D[GW_VM_BORDERBOTTOM], layoutW, perfectHeight);
      layoutH -= south.vmData[GW_VM_H] + this.layoutData[L_DOWN_GAP];
      perfectHeight = layoutH;
    }

    if (west != null) {
      if (west instanceof GContainer) west.layout2();
      perfectWidth = this.getPerfectWidth(west, layoutW);
      if (perfectHeight === 0) perfectHeight = this.getPerfectHeight(west, perfectWidth, layoutH);
      west.setBounds(layoutX, layoutY, perfectWidth, perfectHeight);
      layoutX = west.vmData[GW_VM_W] + this.layoutData[L_LEFT_GAP];
      layoutW -= layoutX;
      perfectWidth = layoutW;
      perfectHeight = layoutH;
    }

    if (east != null) {
      if (east instanceof GContainer) east.layout2();
      perfectWidth = this.getPerfectWidth(east, layoutW);
      if (perfectHeight === 0) perfectHeight = this.getPerfectHeight(east, perfectWidth, layoutH);
      east.setBounds(D[GW_VM_W] - perfectWidth - D[GW_VM_BORDERRIGHT], layoutY, perfectWidth, perfectHeight);
      layoutW -= east.vmData[GW_VM_W] + this.layoutData[L_RIGHT_GAP];
    }

    if (center != null) {
      center.setBounds(layoutX, layoutY, layoutW, layoutH);
      if (center instanceof GContainer) center.layout2();
      center.setBounds(layoutX, layoutY, layoutW, layoutH);
    }

    if (layoutW === 0) {
      let width = 0;
      if (north != null) width = north.vmData[GW_VM_W];
      if (south != null && width < south.vmData[GW_VM_W]) width = south.vmData[GW_VM_W];
      let width2 = this.layoutData[L_LEFT_GAP] + this.layoutData[L_RIGHT_GAP];
      if (west != null) width2 += west.vmData[GW_VM_W];
      if (east != null) width2 += east.vmData[GW_VM_W];
      if (center != null) width2 += center.vmData[GW_VM_W];
      if (width < width2) width = width2;
      D[GW_VM_MIN_WIDTH] = width;
    }

    if (layoutH === 0) {
      let height = 0;
      if (west != null) height = west.vmData[GW_VM_H];
      if (east != null && height < east.vmData[GW_VM_H]) height = east.vmData[GW_VM_H];
      if (center != null && height < center.vmData[GW_VM_H]) height = center.vmData[GW_VM_H];
      height += this.layoutData[L_UP_GAP] + this.layoutData[L_DOWN_GAP];
      if (north != null) height += north.vmData[GW_VM_H];
      if (south != null) height += south.vmData[GW_VM_H];
      D[GW_VM_MIN_HEIGHT] = height;
    }
  }
}

/**
 * GWindow — adds focus handling and the CYCLE/CYCLEUI/PAINT/PROCESSPACKET call
 * stacks that fan a window's callbacks out over its children.
 */
export class GWindow extends GContainer {
  /** statics shared by every window, exactly like the Java statics */
  static pressWidget = null;
  static dropTargetWidget = null;
  static isDragging = false;
  static uiMaxWidth = 0;
  static uiMaxHeight = 0;
  static uiLeft = 0;
  static uiTop = 0;
  static forcePaintWorld = false;

  constructor(vmGame, self, vmData, isTransparent, name) {
    super(vmGame, self, vmData, name);
    this.isTransparent = isTransparent;
    this.funcCycle = null;
    this.funcCycleUI = null;
    this.funcPaint = null;
    this.funcPacket = null;
    this.focusWidget = null;
    this.reCreateStackFlag = true;
    this.catchInput = false;
    this.ignorePauseUICycle = false;
    this.fullScreen = false;
    this.inJavaObjStack = [null, null, null, null];
    this.inVmObjStack = [null, null, null, null];
    this.funcStack = [null, null, null, null];
    this.paintType = null;
  }

  setReCreateStack() {
    this.reCreateStackFlag = true;
  }

  setCatchInput(catchInput) {
    this.catchInput = catchInput;
  }

  setFocus(gWidget) {
    if (gWidget == null) return;
    const old = this.focusWidget;
    this.focusWidget = gWidget;

    if (old != null && this.focusWidget != null && this.focusWidget.parent != null) {
      this.focusWidget.parent.setSrollBar(old, this.focusWidget);
    }

    if (old != null) this.sendFocusEvent(old, false);
    this.sendFocusEvent(this.focusWidget, true);
  }

  move(offsetX, offsetY) {
    this.vmData[GW_VM_XX] += offsetX;
    this.vmData[GW_VM_YY] += offsetY;
    this.setPos(this.vmData[GW_VM_X] + offsetX, this.vmData[GW_VM_Y] += offsetY);
  }

  sendFocusEvent(gWidget, isFocus) {
    gWidget.isFocus = isFocus;
    const eventType = isFocus ? GW_EVENT_GET_FOCUS : GW_EVENT_LOST_FOCUS;
    if (gWidget.vmData[GW_VM_FUNC_SEND_EVENT] !== 0) {
      this.vmGame.gtvm.callback(gWidget.vmData[GW_VM_FUNC_SEND_EVENT],
        [gWidget.vmData[GW_VM_SELF], eventType, 0, 0, 0, 0]);
    }
  }

  /**
   * handleCaller — build the flat call stack for one cycle type and fire the
   * window's callback with it. Ported from GWindow.handleCaller.
   * @param {number} type VM.CYCLE | CYCLEUI | PAINT | PROCESSPACKET
   * @param {boolean} blocked whether the owning VM is currently paused
   */
  handleCaller(type, blocked) {
    const objStack = [];
    const vmFuncStack = [];
    const paintTypeStack = [];
    const curStackId = type - 1;

    let funcName = null;
    let funcId = 0;

    switch (type) {
      case 0: funcName = this.funcCycle; funcId = this.vmData[GW_VM_FUNC_CYCLE]; break; // CYCLE
      case 1: // CYCLEUI
        if ((!blocked || this.ignorePauseUICycle) && this.isShow) {
          funcName = this.funcCycleUI;
          funcId = this.vmData[GW_VM_FUNC_CYCLEUI];
          this.handleCycleUI(this.focusWidget, objStack, vmFuncStack);
          this.resetCallStack(type, objStack, vmFuncStack, paintTypeStack, curStackId);
        } else {
          return;
        }
        break;
      case 3: // PAINT
        if (this.isShow) {
          if (this.isJavaPaint) {
            this.paintContainer();
          } else {
            funcName = this.funcPaint;
            funcId = this.vmData[GW_VM_FUNC_PAINT];
          }
        } else {
          return;
        }
        break;
      case 2: funcName = this.funcPacket; funcId = this.vmData[GW_VM_FUNC_PACKET]; break; // PROCESSPACKET
    }

    if (type === 3 && this.reCreateStackFlag) {
      this.createCallStack(type, funcId, objStack, vmFuncStack, paintTypeStack);
      this.resetCallStack(type, objStack, vmFuncStack, paintTypeStack, curStackId);
      this.reCreateStackFlag = false;
    } else if (type === 0 || type === 2) {
      this.createCallStack(type, funcId, objStack, vmFuncStack, paintTypeStack);
      this.resetCallStack(type, objStack, vmFuncStack, paintTypeStack, curStackId);
    }

    if (funcName != null) {
      const gtvm = this.vmGame.gtvm;
      if (this.children.length === 0) {
        gtvm.callback(funcName, [this.vmData[GW_VM_SELF]]);
      } else if (this.inJavaObjStack[curStackId] != null && this.inJavaObjStack[curStackId].length > 0) {
        if (type === 3) {
          gtvm.callback(funcName, [
            this.vmData[GW_VM_SELF],
            gtvm.makeTempObject(this.inJavaObjStack[curStackId]),
            gtvm.makeTempObject(this.inVmObjStack[curStackId]),
            gtvm.makeTempObject(this.funcStack[curStackId]),
            gtvm.makeTempObject(this.paintType),
            this.inJavaObjStack[curStackId].length,
          ]);
        } else {
          gtvm.callback(funcName, [
            this.vmData[GW_VM_SELF],
            gtvm.makeTempObject(this.inJavaObjStack[curStackId]),
            gtvm.makeTempObject(this.inVmObjStack[curStackId]),
            gtvm.makeTempObject(this.funcStack[curStackId]),
            this.inJavaObjStack[curStackId].length,
          ]);
        }
      }
    }
  }

  createCallStack(type, funcId, objStack, vmFuncStack, paintTypeStack) {
    if (type === 3 && this.vmData[GW_VM_FUNC_PAINT_BEFORE] > 0) {
      objStack.push(this);
      vmFuncStack.push(this.vmData[GW_VM_FUNC_PAINT_BEFORE]);
      paintTypeStack.push(PAINT_TYPE_IN_VM);
    }

    if (funcId !== 0 && this.isShow) {
      objStack.push(this);
      vmFuncStack.push(funcId);
      if (type === 3) paintTypeStack.push(PAINT_TYPE_IN_VM);
    }

    this.setCallStack(type, objStack, vmFuncStack, paintTypeStack);

    if (type === 3 && this.vmData[GW_VM_FUNC_PAINT_AFTER] > 0) {
      objStack.push(this);
      vmFuncStack.push(this.vmData[GW_VM_FUNC_PAINT_AFTER]);
      paintTypeStack.push(PAINT_TYPE_AFTER);
    }
  }

  /** GContainer.setCallStack — walk children collecting their callbacks. */
  setCallStack(type, vmObjStack, vmFuncStack, paintTypeStack) {
    for (const child of this.children) {
      if (!child.isShow || child.isOutView()) continue;
      if (type === 3) this.addPaintObj(child, vmObjStack, vmFuncStack, paintTypeStack);
      else if (type === 0) this.addCycleObj(child, vmObjStack, vmFuncStack);
      else if (type === 2) this.addPacketObj(child, vmObjStack, vmFuncStack);
    }
  }

  addPaintObj(w, objStack, funcStack, paintTypeStack) {
    if (w.vmData[GW_VM_FUNC_PAINT_BEFORE] > 0) {
      objStack.push(w); funcStack.push(w.vmData[GW_VM_FUNC_PAINT_BEFORE]); paintTypeStack.push(PAINT_TYPE_IN_VM);
    }
    if (w.vmData[GW_VM_FUNC_PAINT] > 0) {
      objStack.push(w); funcStack.push(w.vmData[GW_VM_FUNC_PAINT]); paintTypeStack.push(PAINT_TYPE_IN_VM);
    }
    if (w instanceof GContainer) w.setCallStack(3, objStack, funcStack, paintTypeStack);
    if (w.vmData[GW_VM_FUNC_PAINT_AFTER] > 0) {
      objStack.push(w); funcStack.push(w.vmData[GW_VM_FUNC_PAINT_AFTER]); paintTypeStack.push(PAINT_TYPE_AFTER);
    }
  }

  addCycleObj(w, objStack, funcStack) {
    if (w.vmData[GW_VM_FUNC_CYCLE] > 0) {
      objStack.push(w); funcStack.push(w.vmData[GW_VM_FUNC_CYCLE]);
    }
    if (w instanceof GContainer) w.setCallStack(0, objStack, funcStack, null);
  }

  addPacketObj(w, objStack, funcStack) {
    if (w.vmData[GW_VM_FUNC_PACKET] > 0) {
      objStack.push(w); funcStack.push(w.vmData[GW_VM_FUNC_PACKET]);
    }
    if (w instanceof GContainer) w.setCallStack(2, objStack, funcStack, null);
  }

  resetCallStack(type, objStack, vmFuncStack, paintTypeStack, curStackId) {
    const count = objStack.length;
    this.inJavaObjStack[curStackId] = new Array(count);
    this.inVmObjStack[curStackId] = new Array(count);
    this.funcStack[curStackId] = new Int32Array(count);
    if (type === 3) this.paintType = new Int32Array(count);
    for (let i = 0; i < count; i++) {
      const gw = objStack[i];
      this.inJavaObjStack[curStackId][i] = gw;
      this.inVmObjStack[curStackId][i] = gw.vmData;
      this.funcStack[curStackId][i] = vmFuncStack[i];
      if (type === 3) this.paintType[i] = paintTypeStack[i];
    }
  }

  handleCycleUI(focusWidget, vmObjStack, vmFuncStack) {
    if (focusWidget != null) {
      if (focusWidget.vmData[GW_VM_FUNC_CYCLEUI] > 0) {
        vmObjStack.push(focusWidget);
        vmFuncStack.push(focusWidget.vmData[GW_VM_FUNC_CYCLEUI]);
      }
      if (focusWidget.parent != null) {
        this.handleCycleUI(focusWidget.parent, vmObjStack, vmFuncStack);
      }
    }
  }

  getVMGame() {
    return this.vmGame;
  }

  canHandleCycleUI(gWidget) {
    if (this.focusWidget != null) {
      if (gWidget === this.focusWidget) return true;
      let p = this.focusWidget.parent;
      while (p != null) {
        if (p === gWidget) return true;
        p = p.parent;
      }
    }
    return false;
  }
}

/**
 * GScrollBar — only the state the container and the 0x1256..0x1267 syscalls
 * touch; painting comes later with the resource layer.
 */
export class GScrollBar extends GWidget {
  static GSB_ALIGN_CENTER = 0;
  static GSB_ALIGN_RIGHT = 1;

  constructor(vmGame, self, vmData, name) {
    super(vmGame, self, vmData, name);
    this.align = GScrollBar.GSB_ALIGN_CENTER;
    this.scrollPos = 0;
    this.maxScrollDis = 0;
    this.tick = 0;
  }

  reset() {
    this.scrollPos = 0;
    this.maxScrollDis = 0;
  }

  getMaxScrollDis() { return this.maxScrollDis; }
  setMaxScrollDis(v) { this.maxScrollDis = v; }
  getScrollPos() { return this.scrollPos; }
  setScrollPos(v) { this.scrollPos = v; }
  getTick() { return this.tick; }
  setAlign(a) { this.align = a; }
}