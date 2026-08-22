/*
 * Resource syscall layer — the ImageSet/AnimateSet/Res-animate cases the boot
 * path touches (0x0031..0x004C, 0x1302..0x1318, 0x1326).
 *
 * FAIL LOUD: a missing resource or an unported case here throws, so the boot
 * smoke test reports script/function/id instead of rendering a silent hole.
 * (Java's ImageSet_Create caught IO errors and returned 0 — that behaviour is
 * kept ONLY where the original really did swallow, marked per case.)
 */
import { UNHANDLED } from './syscalls-core.js';
import { PipImage } from '../assets/pip-image.js';
import { PipAnimateSet } from '../assets/pip-animate-set.js';
import { VMAnimateSet, VMAnimatePlayer } from './resources.js';

export class ResHost {
  /**
   * @param {import('./resources.js').ResourceStore} store
   */
  constructor(store) {
    this.store = store;
    /** GameMain.clientAnimates equivalent: deamon index -> VMAnimatePlayer */
    this.daemonPlayers = new Map();
    /** GameMain.specialAnimates equivalent: index -> VMAnimateSet */
    this.specialAnimates = new Map();
    /** GameMain.initializing / GameMain.resetType equivalents */
    this.initializing = false;
    this.resetType = 0;
  }

  handle(vm, funcID, params) {
    const S = this.store;
    switch (funcID) {
      /* -------------------------------------------------- AnimateSet */
      case 0x0031: { // AnimateSet_Create(ImageSet[] imgs, String ctnFile)
        const imgs = vm.followPointer(params[0]) ?? [];
        const ctnFile = String(vm.followPointer(params[1]) ?? '');
        const bytes = S._cachedBytes(ctnFile);
        if (bytes == null) throw new Error(`AnimateSet_Create: ctn not preloaded: "${ctnFile}"`);
        return vm.makeTempObject(makeAnimate(S, bytes, imgs.map((s) => s?.pip ?? null)));
      }
      case 0x0035: { // AnimateSet_Create2(ImageSet[] imgs, byte[] ctnData)
        const imgs = vm.followPointer(params[0]) ?? [];
        const data = vm.followPointer(params[1]);
        return vm.makeTempObject(makeAnimate(S, data, imgs.map((s) => s?.pip ?? null)));
      }
      case 0x0032: { // AnimateSet_DrawFrame(set, g, frame, x, y)
        const set = vm.followPointer(params[0]);
        set?.drawFrame(vm.followPointer(params[1]), params[2], params[3], params[4]);
        return 0;
      }
      case 0x0033: { // AnimateSet_DrawAnimate(set, g, index, tick, x, y)
        const set = vm.followPointer(params[0]);
        set?.drawAnimateFrame(vm.followPointer(params[1]), params[2], params[3], params[4], params[5]);
        return 0;
      }
      case 0x0034: { // AnimateSet_GetAnimateLength(set, index)
        const set = vm.followPointer(params[0]);
        return set ? set.getAnimateLength(params[1]) : 0;
      }
      case 0x0036: { // AnimateSet_GetAnimateBox(set, index) -> int[4]
        const set = vm.followPointer(params[0]);
        return set ? vm.makeTempObject(set.getAnimateBox(params[1])) : 0;
      }

      /* ----------------------------------------------------- ImageSet */
      case 0x0041: // ImageSet_Create(fileName) — Java caught IO errors -> 0
      case 0x0042: // ImageSet_Create1(fileName, rows, cols) — grid sheets decode the same
      case 0x0043: { // ImageSet_Create2(fileName)
        const name = String(vm.followPointer(params[0]) ?? '');
        try {
          return vm.makeTempObject(S.imageSetSync(name));
        } catch (err) {
          if (isMissing(err)) return 0;
          throw new Error(`ImageSet_Create("${name}"): ${err.message}`);
        }
      }
      case 0x0044: { // ImageSet_Create4(byte[] imgData)
        try {
          return vm.makeTempObject(new VMImageSetWrapper(vm.followPointer(params[0]), S));
        } catch (err) {
          if (isMissing(err)) return 0;
          throw err;
        }
      }
      case 0x0045: { // ImageSet_DrawFrame(obj, g, frame, x, y, anchor) — trans 0
        const img = vm.followPointer(params[0]);
        img?.drawFrame(vm.followPointer(params[1]), params[2], params[3], params[4], 0, params[5]);
        return 0;
      }
      case 0x004c: { // ImageSet_DrawFrame2(obj, g, frame, x, y, trans, anchor)
        const img = vm.followPointer(params[0]);
        img?.drawFrame(vm.followPointer(params[1]), params[2], params[3], params[4], params[5], params[6]);
        return 0;
      }
      case 0x0046: { // ImageSet_GetFrameWidth(obj, frame)
        const img = vm.followPointer(params[0]);
        return img ? img.getFrameWidth(params[1]) : 0;
      }
      case 0x0047: { // ImageSet_GetFrameHeight(obj, frame)
        const img = vm.followPointer(params[0]);
        return img ? img.getFrameHeight(params[1]) : 0;
      }
      case 0x0048: vm.followPointer(params[0])?.gray(); return 0;
      case 0x0049: vm.followPointer(params[0])?.lighter(params[1]); return 0;
      case 0x004a: vm.followPointer(params[0])?.darker(params[1]); return 0;
      case 0x004b: vm.followPointer(params[0])?.mask(params[1]); return 0;

      case 0x1309: // ResSetInitializtion(int initializing)
        this.initializing = params[0] === 1;
        return 0;
      case 0x3005: // ResetClient(int type) — record the reset type
        this.resetType = params[0];
        return 0;
      case 0x3008: // GetResetType()
        return this.resetType;

      /* --------------------------------------------------- Res animate */
      case 0x1302: { // ResCreateAnimateSet(Object[] images, byte[] ctnData)
        const imgs = vm.followPointer(params[0]) ?? [];
        const data = vm.followPointer(params[1]);
        return vm.makeTempObject(makeAnimate(S, data, imgs.map((s) => s?.pip ?? null)));
      }
      case 0x1303: { // ResCreateAnimatePlayer(String animateName, Object animateSet)
        const set = vm.followPointer(params[1]);
        return vm.makeTempObject(new VMAnimatePlayer(set));
      }
      case 0x1304: { // ResInitDeamonAnimatePlayer(player, deamonIndex)
        const player = vm.followPointer(params[0]);
        player?.setAnimate(params[1]);
        player?.setShown(true);
        return 0;
      }
      case 0x1310: { // ResGetDeamonAnimatePlayerCopy(deamonIndex)
        const src = this.daemonPlayers.get(params[0]);
        if (!src) throw new Error(`ResGetDeamonAnimatePlayerCopy: no daemon ${params[0]}`);
        const copy = new VMAnimatePlayer(src.animateSet);
        copy.setAnimate(src.animateIndex);
        return vm.makeTempObject(copy);
      }
      case 0x1318: { // ResGetAnimatePlayerBox(player, index) -> int[4]
        const player = vm.followPointer(params[0]);
        if (!player?.animateSet) return 0;
        return vm.makeTempObject(player.animateSet.getAnimateBox(params[1]));
      }
      case 0x1326: { // ResAnimatePlayerDraw(player, g, x, y)
        const player = vm.followPointer(params[0]);
        player?.draw(vm.followPointer(params[1]), params[2], params[3]);
        return 0;
      }

      default:
        return UNHANDLED;
    }
  }
}

/** an ImageSet built from raw bytes handed in by the script (0x0044) */
class VMImageSetWrapper {
  constructor(bytes, store) {
    // decode now, wrap like imageSetSync does
    const pip = store.makePip(bytes);
    this.pip = pip;
    this.getFrameWidth = (f) => pip.getWidth(f);
    this.getFrameHeight = (f) => pip.getHeight(f);
    const cache = new Map();
    this.drawFrame = (g, frame, x, y, trans = 0, anchor = 0) => {
      const key = `${frame}:${trans}`;
      let img = cache.get(key);
      if (img === undefined) {
        try {
          const rgba = pip.frameToRGBA(frame, trans);
          img = rgba.width > 0 && rgba.height > 0 ? store.rasterise(rgba) : null;
        } catch {
          img = null;
        }
        cache.set(key, img);
      }
      if (img != null) g.drawImage(img, x, y, anchor);
    };
  }
}

/** a bare .ctn/.anp definition plus its already-decoded sheets, script-wrapped */
function makeAnimate(store, bytes, pips, name = '<inline>') {
  try {
    return new VMAnimateSet(new PipAnimateSet(pips, bytes), store.rasterise);
  } catch (err) {
    throw new Error(`AnimateSet_Create("${name}"): ${err.message}`);
  }
}

function isMissing(err) {
  return err && (err.name === 'MissingResourceError' || err.kind === 'ImageSet');
}