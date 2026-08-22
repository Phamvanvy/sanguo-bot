/*
 * UI syscall layers — the drawing block (0x0011..0x001E, 0x201E.., 0x571D..)
 * and the widget block (0x12xx + the GWindow statics in 0x5705..0x571F).
 *
 * Both layers return UNHANDLED for ids they do not own so composeHost() can
 * stack them behind CoreHost.
 *
 * The widget layer talks to `vm.owner`, which must be a VMGame exposing:
 *   gtvm, putGWidget(gw), removeGWidget(gw), getGWidget(key), viewWidth
 */
import { UNHANDLED } from './syscalls-core.js';
import { Font, createImage } from './gfx.js';
import {
  GWidget, GContainer, GWindow, GScrollBar,
  GW_VM_JAVA_GWIDGET, GW_VM_SELF,
} from './widgets.js';

const TRUE = 1;
const FALSE = 0;

/* ------------------------------------------------------------------ gfx */

export class GfxHost {
  constructor(platform) {
    this.platform = platform;
  }

  handle(vm, funcID, params) {
    const P = this.platform;
    switch (funcID) {
      case 0x0011: vm.followPointer(params[0]).fillRect(params[1], params[2], params[3], params[4]); return 0;
      case 0x0012: vm.followPointer(params[0]).drawString(String(vm.followPointer(params[1]) ?? ''), params[2], params[3], params[4]); return 0;
      case 0x0013: vm.followPointer(params[0]).setColor(params[1]); return 0;
      case 0x120e: { // DrawMixedString(g, str, x, y, color, is3D, anchor)
        // mixed-colour markup parsing lands with the text layer; the boot
        // path only needs the string on screen in the base colour
        const g = vm.followPointer(params[0]);
        if (g) {
          g.setColor(params[4]);
          g.drawString(String(vm.followPointer(params[1]) ?? ''), params[2], params[3], params[6]);
        }
        return 0;
      }
      case 0x2020: { // Draw3DString(g, s, x, y, frontColor, bgColor, anchor)
        const g = vm.followPointer(params[0]);
        if (g) {
          const text = String(vm.followPointer(params[1]) ?? '');
          g.setColor(params[5]);
          g.drawString(text, params[2] + 1, params[3] + 1, params[6]);
          g.setColor(params[4]);
          g.drawString(text, params[2], params[3], params[6]);
        }
        return 0;
      }
      case 0x0014: vm.followPointer(params[0]).drawRect(params[1], params[2], params[3], params[4]); return 0;
      case 0x0015: vm.followPointer(params[0]).setClip(params[1], params[2], params[3], params[4]); return 0;
      case 0x0016: return P.screenWidth();
      case 0x0017: return P.screenHeight();
      case 0x0018: vm.followPointer(params[0]).drawRoundRect(params[1], params[2], params[3], params[4], params[5], params[6]); return 0;
      case 0x0019: return vm.makeTempObject(P.graphics);
      case 0x001a: return vm.makeTempObject(createImage(params[0], params[1]));
      case 0x001b: return vm.makeTempObject(vm.followPointer(params[0]).getGraphics());
      case 0x001c: return P.fontHeight();
      case 0x001d: return P.font.stringWidth(String(vm.followPointer(params[0]) ?? ''));
      case 0x001e: vm.followPointer(params[0]).drawImage(vm.followPointer(params[1]), params[2], params[3], params[4]); return 0;
      case 0x0020: return P.lineHeight();

      case 0x201e: vm.followPointer(params[0]).drawLine(params[1], params[2], params[3], params[4]); return 0;
      case 0x201f: vm.followPointer(params[0]).fillRoundRect(params[1], params[2], params[3], params[4], params[5], params[6]); return 0;
      case 0x2039: vm.followPointer(params[0]).setStrokeStyle(params[1] !== 0 ? 1 : 0); return 0;

      case 0x00ce: { // FillAlphaRect — argb colour with alpha channel
        const g = vm.followPointer(params[0]);
        const argb = params[1] >>> 0;
        const a = ((argb >>> 24) & 0xff) / 255;
        const hex = `#${(argb & 0xffffff).toString(16).padStart(6, '0')}`;
        const c = g.ctx;
        const oldAlpha = c.globalAlpha;
        c.globalAlpha = a;
        c.fillStyle = hex;
        g.fillRect(params[2], params[3], params[4], params[5]);
        c.globalAlpha = oldAlpha;
        return 0;
      }

      case 0x571d: vm.followPointer(params[0]).fillTriangle(params[1], params[2], params[3], params[4], params[5], params[6]); return 0;
      case 0x571e: { // GetClip -> int[4]
        const g = vm.followPointer(params[0]);
        if (g == null) return 0;
        return vm.makeTempObject(Int32Array.of(g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight()));
      }
      case 0x5750: return vm.followPointer(params[0]).getHeight();          // GetFontHeightEx
      case 0x5751: return vm.followPointer(params[1]).stringWidth(String(vm.followPointer(params[0]) ?? '')); // StringWidthEx
      case 0x5754: vm.followPointer(params[0]).setFont(vm.followPointer(params[1])); return 0; // SetFont
      case 0x5755: return vm.makeTempObject(Font.getFont(params[0], params[1], params[2]));    // GetFont
      case 0x5756: vm.followPointer(params[0]).setFont(P.fontObj ?? null); return 0;           // ReSetFont
      case 0x5759: return vm.makeTempObject(vm.followPointer(params[1])?.getFont() ?? null);   // GetCurrentFont

      default:
        return UNHANDLED;
    }
  }
}

/* --------------------------------------------------------------- widgets */

export class WidgetHost {
  constructor(platform) {
    this.platform = platform;
  }

  /** follow a script-side widget struct to its Java-side object */
  _gw(vm, ptr) {
    const data = vm.followPointer(ptr);
    if (data == null) return null;
    const vmGame = vm.owner;
    return vmGame ? vmGame.getGWidget(data[GW_VM_JAVA_GWIDGET]) : null;
  }

  _selfPtr(gWidget) {
    return gWidget != null ? gWidget.vmData[GW_VM_SELF] : 0;
  }

  handle(vm, funcID, params) {
    const owner = vm.owner; // the VMGame these widgets belong to
    if (!owner || typeof owner.getGWidget !== 'function') return UNHANDLED;

    switch (funcID) {
      /* ------------------------------------------- creation/destruction */
      case 0x1230: { // CreateGWindow(self, isTransparent, name)
        const win = new GWindow(owner, params[0], vm.followPointer(params[0]),
          params[1] === TRUE, String(vm.followPointer(params[2]) ?? ''));
        return win.vmData[GW_VM_SELF];
      }
      case 0x1231: owner.vmDestroyWindow?.(this._gw(vm, params[0])); return 0;
      case 0x1232: owner.vmShowWindow?.(this._gw(vm, params[0])); return 0;
      case 0x1233: { // CreateGContainer
        const c = new GContainer(owner, params[0], vm.followPointer(params[0]), String(vm.followPointer(params[1]) ?? ''));
        return c.vmData[GW_VM_SELF];
      }
      case 0x1234: { // GContainerAdd
        const parent = this._gw(vm, params[0]);
        const child = this._gw(vm, params[1]);
        if (parent instanceof GContainer && child) parent.add(child);
        return 0;
      }
      case 0x1235: { // GContainerDel
        const parent = this._gw(vm, params[0]);
        const child = this._gw(vm, params[1]);
        if (parent instanceof GContainer && child) parent.remove(child);
        return 0;
      }
      case 0x1236: owner.vmCloseWindow?.(this._gw(vm, params[0])); return 0;
      case 0x1237: { // CreateGWidget
        const w = new GWidget(owner, params[0], vm.followPointer(params[0]), String(vm.followPointer(params[1]) ?? ''));
        return w.vmData[GW_VM_SELF];
      }
      case 0x1238: { // GContainerInsert
        const parent = this._gw(vm, params[0]);
        const child = this._gw(vm, params[1]);
        if (parent instanceof GContainer && child) parent.insert(child, params[2]);
        return 0;
      }
      case 0x12ad: { // DestroyGWidget — fire the script's destroy hook, then free
        const w = this._gw(vm, params[0]);
        if (w != null) {
          if (w.vmData[GW_VM_FUNC_DESTROY] > 0) {
            owner.gtvm.callback(w.vmData[GW_VM_FUNC_DESTROY], [w.vmData[GW_VM_SELF]]);
          }
          w.freeVMObj();
          owner.removeGWidget(w);
        }
        return 0;
      }

      /* ------------------------------------------------------ geometry */
      case 0x1239: { const w = this._gw(vm, params[0]); if (w) w.setBounds(params[1], params[2], params[3], params[4]); return 0; }
      case 0x123a: { const w = this._gw(vm, params[0]); if (w) w.setBorder(params[1], params[2], params[3], params[4]); return 0; }
      case 0x1255: { const w = this._gw(vm, params[0]); if (w) w.setPos(params[1], params[2]); return 0; }
      case 0x128b: { const w = this._gw(vm, params[0]); if (w) w.move(params[1], params[2]); return 0; }
      case 0x1284: { const w = this._gw(vm, params[0]); return w ? w.getAbsX() : 0; }
      case 0x1285: { const w = this._gw(vm, params[0]); return w ? w.getAbsY() : 0; }
      case 0x1288: { const w = this._gw(vm, params[0]); if (w) w.setPressXY(params[1], params[2]); return 0; }
      case 0x1289: { const w = this._gw(vm, params[0]); return w ? w.getPressX() : 0; }
      case 0x128a: { const w = this._gw(vm, params[0]); return w ? w.getPressY() : 0; }

      /* -------------------------------------------------------- layout */
      case 0x123b: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.setLayoutMode(params[1], params[2], params[3], params[4], params[5], params[6]); return 0; }
      case 0x123c: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.layout(); return 0; }
      case 0x123d: { const w = this._gw(vm, params[0]); if (w) w.setScale(params[1] === TRUE); return 0; }
      case 0x124a: { const w = this._gw(vm, params[0]); if (w) w.SetNeedLayout(params[1] === TRUE); return 0; }
      case 0x126e: { const w = this._gw(vm, params[0]); if (w) w.setGrid3Data(params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8]); return 0; }
      case 0x126f: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.grid3Layout(); return 0; }
      case 0x1270: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.borderLayout(); return 0; }
      case 0x12b5: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.setHLayout(params[1], params[2]); return 0; }
      case 0x12b6: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.setVLayout(params[1], params[2]); return 0; }
      case 0x12b7: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.setGridLayout(params[1], params[2]); return 0; }
      case 0x12b8: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.setGrid2Layout(params[1], params[2], params[3], params[4]); return 0; }
      case 0x12b9: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.setGrid3Layout(params[1], params[2]); return 0; }
      case 0x12ba: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.setBorderLayout(params[1], params[2], params[3], params[4]); return 0; }
      case 0x1287: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.isIntersectView = params[1] === TRUE; return 0; }
      case 0x1268: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.setIsJavaPaint(params[1] === TRUE); return 0; }
      case 0x1283: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.setAbs(); return 0; }

      /* ------------------------------------------------------ children */
      case 0x129c: { const w = this._gw(vm, params[0]); return w instanceof GContainer ? vm.makeTempObject(w.getChildren()) : 0; }
      case 0x129d: { const w = this._gw(vm, params[0]); return w instanceof GContainer ? vm.makeTempObject(w.getChild(params[1])) : 0; }
      case 0x129e: { const w = this._gw(vm, params[0]); return w instanceof GContainer ? w.children.length : 0; }
      case 0x129f: { const w = this._gw(vm, params[0]); const t = this._gw(vm, params[1]); if (w instanceof GContainer && t) w.batchAdd(t, params[2]); return 0; }
      case 0x12a0: { const w = this._gw(vm, params[0]); return w instanceof GContainer ? vm.makeTempObject(w.children.slice()) : 0; }
      case 0x12a1: { const w = this._gw(vm, params[0]); return w ? vm.makeTempObject(GWidget.getCloneArray(owner, w, params[1])) : 0; }
      case 0x12a2: { const w = this._gw(vm, params[0]); return w instanceof GContainer ? vm.makeTempObject(w.getJavaChildren()) : 0; }
      case 0x12a3: { const w = this._gw(vm, params[0]); return w instanceof GContainer ? vm.makeTempObject(w.getJavaChild(params[1])) : 0; }
      case 0x129b: { const w = this._gw(vm, params[0]); return w ? w.getClone(owner).vmData[GW_VM_SELF] : 0; }
      case 0x1271: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.toTop(params[1]); return 0; }
      case 0x12cf: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.toBottom(params[1]); return 0; }
      case 0x12d0: { const c = this._gw(vm, params[0]); const w = this._gw(vm, params[1]); return c instanceof GContainer && w ? c.getIndex(w) : -1; }
      case 0x1246: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.clear(); return 0; }
      case 0x126c: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.setChildrenOffset(params[1], params[2]); return 0; }
      case 0x12dc: { const w = this._gw(vm, params[0]); return w instanceof GContainer ? w.firstInViewIndex : 0; }
      case 0x12dd: { const w = this._gw(vm, params[0]); return w instanceof GContainer ? w.lastInViewIndex : 0; }

      /* ------------------------------------------------------- scroll */
      case 0x1254: { const c = this._gw(vm, params[0]); const sb = this._gw(vm, params[1]); if (c instanceof GContainer && sb instanceof GScrollBar) c.addScrollBar(sb); return 0; }
      case 0x1256: { const sb = new GScrollBar(owner, params[0], vm.followPointer(params[0]), String(vm.followPointer(params[1]) ?? '')); return sb.vmData[GW_VM_SELF]; }
      case 0x1257: { const w = this._gw(vm, params[0]); return w instanceof GScrollBar ? w.getMaxScrollDis() : 0; }
      case 0x1258: { const w = this._gw(vm, params[0]); return w instanceof GScrollBar ? w.getScrollPos() : 0; }
      case 0x1259: { const w = this._gw(vm, params[0]); return w instanceof GScrollBar ? w.getTick() : 0; }
      case 0x125b: { const w = this._gw(vm, params[0]); if (w instanceof GScrollBar) w.setAlign(params[1]); return 0; }
      case 0x125c: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.moveUp(); return 0; }
      case 0x125d: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.moveDown(); return 0; }
      case 0x125e: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.moveUpPage(); return 0; }
      case 0x1265: { const w = this._gw(vm, params[0]); return w instanceof GContainer && w.needScrollBar ? TRUE : FALSE; }
      case 0x1266: { const w = this._gw(vm, params[0]); if (w instanceof GScrollBar) w.setMaxScrollDis(params[1]); return 0; }
      case 0x1267: { const w = this._gw(vm, params[0]); if (w instanceof GScrollBar) w.setScrollPos(params[1]); return 0; }
      case 0x5710: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.resetScrollBar(); return 0; }
      case 0x5717: { // GGetScrollBar
        const w = this._gw(vm, params[0]);
        const sb = w instanceof GContainer ? w.getScrollBar() : null;
        return sb != null ? sb.vmData[GW_VM_SELF] : 0;
      }
      case 0x571f: { const w = this._gw(vm, params[0]); if (w instanceof GContainer) w.needScrollBar = params[1] === TRUE; return 0; }

      /* ------------------------------------------------- focus/window */
      case 0x124b: { const win = this._gw(vm, params[0]); const w = this._gw(vm, params[1]); if (win instanceof GWindow) win.setFocus(w); return 0; }
      case 0x124c: { const w = this._gw(vm, params[0]); return this._selfPtr(w ? w.getParentWindow() : null); }
      case 0x124d: { const w = this._gw(vm, params[0]); return w && w.isFocused() ? TRUE : FALSE; }
      case 0x124e: { const w = this._gw(vm, params[0]); if (w) w.setEnableFocus(params[1] === TRUE); return 0; }
      case 0x12a4: { const w = this._gw(vm, params[0]); return w instanceof GWindow ? this._selfPtr(w.focusWidget) : 0; }
      case 0x12a5: { const w = this._gw(vm, params[0]); return w instanceof GWindow ? vm.makeTempObject(w.focusWidget) : 0; }
      case 0x12a6: { const win = this._gw(vm, params[0]); const w = this._gw(vm, params[1]); return win instanceof GWindow && win.canHandleCycleUI(w) ? TRUE : FALSE; }
      case 0x1272: return GWindow.pressWidget != null ? GWindow.pressWidget.vmData[GW_VM_SELF] : 0;
      case 0x127b: { GWindow.pressWidget = this._gw(vm, params[0]); return 0; }
      case 0x127e: return GWindow.pressWidget != null ? vm.makeTempObject(GWindow.pressWidget) : 0;
      case 0x1292: { const w = this._gw(vm, params[0]); if (w instanceof GWindow) w.setReCreateStack(); return 0; }
      case 0x1293: { const w = this._gw(vm, params[0]); if (w instanceof GWindow) w.isTransparent = params[1] === TRUE; return 0; }
      case 0x12af: { const w = this._gw(vm, params[0]); if (w instanceof GWindow) w.catchInput = params[1] === TRUE; return 0; }
      case 0x12d3: { const w = this._gw(vm, params[0]); if (w instanceof GWindow) w.ignorePauseUICycle = params[1] === TRUE; return 0; }
      case 0x12d4: { const w = this._gw(vm, params[0]); return w instanceof GWindow && w.ignorePauseUICycle ? TRUE : FALSE; }
      case 0x1281: { const w = this._gw(vm, params[0]); return w instanceof GWindow && !w.isShow ? TRUE : FALSE; }
      case 0x12a7: return vm.makeTempObject(owner.getTopUIVMId?.() ?? '');
      case 0x127c: { const w = this._gw(vm, params[0]); return w instanceof GWindow ? vm.makeTempObject(w.getVMGame()) : 0; }
      case 0x12cb: { const w = this._gw(vm, params[0]); return w ? vm.makeTempObject(w.vmGame) : 0; }
      case 0x12cc: { const w = this._gw(vm, params[0]); return vm.makeTempObject(w ? w.name : null); }
      case 0x12ae: { const w = this._gw(vm, params[0]); return vm.makeTempObject(w); }
      case 0x12d1: return vm.makeTempObject(owner);

      /* -------------------------------------------------- show/hide */
      case 0x12a8: { const w = this._gw(vm, params[0]); return w && w.getShow() ? TRUE : FALSE; }
      case 0x12a9: { const w = this._gw(vm, params[0]); if (w) w.setShow(params[1] === TRUE); return 0; }

      /* --------------------------------------------------- drag/drop */
      case 0x1294: return GWindow.isDragging ? TRUE : FALSE;
      case 0x1295: GWindow.isDragging = params[0] === TRUE; return 0;
      case 0x1298: return GWindow.dropTargetWidget != null ? GWindow.dropTargetWidget.vmData[GW_VM_SELF] : 0;
      case 0x1299: return GWindow.dropTargetWidget != null ? vm.makeTempObject(GWindow.dropTargetWidget) : 0;
      case 0x129a: GWindow.dropTargetWidget = this._gw(vm, params[0]); return 0;

      /* ------------------------------------------- window size limits */
      case 0x5705: GWindow.uiMaxWidth = params[0]; return 0;
      case 0x5706: GWindow.uiMaxHeight = params[0]; return 0;
      case 0x5707: return GWindow.uiMaxWidth;
      case 0x5708: return GWindow.uiMaxHeight;
      case 0x5709: GWindow.uiLeft = params[0]; return 0;
      case 0x570a: GWindow.uiTop = params[0]; return 0;
      case 0x570b: return GWindow.uiLeft;
      case 0x570c: return GWindow.uiTop;
      case 0x570d: GWindow.forcePaintWorld = params[0] === TRUE; return 0;
      case 0x5711: { const w = this._gw(vm, params[0]); if (w instanceof GWindow) w.fullScreen = params[1] === TRUE; return 0; }
      case 0x5712: { const w = this._gw(vm, params[0]); return w instanceof GWindow && w.fullScreen ? TRUE : FALSE; }
      case 0x5718: { const w = this._gw(vm, params[0]); return w instanceof GContainer ? TRUE : FALSE; }
      case 0x5719: { const w = this._gw(vm, params[0]); return w instanceof GWindow ? TRUE : FALSE; }
      case 0x571a: { const w = this._gw(vm, params[0]); return w ? vm.makeTempObject(Int32Array.from(w.getIntersect())) : 0; }

      default:
        return UNHANDLED;
    }
  }
}