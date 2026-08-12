/*
 * JavaRandom — port of client/src/com/pip/engine/Random.java, which is itself a copy of
 * java.util.Random's 48-bit linear congruential generator.
 *
 * This is NOT a cosmetic detail. Blurred maps store only a seed; every tile is re-derived by
 * replaying this exact PRNG in this exact call order (GameMap.createBlurMapBuffer). Any drift
 * in the generator -- or in how many times it is called -- produces a different-looking map.
 * BigInt is used for the 48-bit state because the multiply overflows JS's 53-bit float ints.
 */
const MULT = 0x5deece66dn;      // 25214903917
const ADD = 0xbn;               // 11
const MASK = (1n << 48n) - 1n;  // 281474976710655

export class JavaRandom {
  constructor(seed) {
    this.setSeed(seed);
  }

  setSeed(seed) {
    this.seed = (BigInt(seed) ^ MULT) & MASK;
  }

  /** protected int next(int bits) */
  next(bits) {
    this.seed = (this.seed * MULT + ADD) & MASK;
    // Java's (int) cast keeps the low 32 bits as a signed value.
    return Number(BigInt.asIntN(32, this.seed >> BigInt(48 - bits)));
  }

  nextInt(n) {
    if (n === undefined) return this.next(32);
    if (n <= 0) throw new Error('JavaRandom.nextInt: n must be positive');
    if ((n & -n) === n) {
      // power of two: (int)((long)n * (long)next(31) >> 31)
      return Number((BigInt(n) * BigInt(this.next(31))) >> 31n);
    }
    let bits, val;
    do {
      bits = this.next(31);
      val = bits % n;
    } while (((bits - val + (n - 1)) | 0) < 0);
    return val;
  }

  /** Returns a BigInt: the value is a signed 64-bit integer that JS numbers cannot hold. */
  nextLong() {
    const hi = BigInt(this.next(32));
    const lo = BigInt(this.next(32));
    return BigInt.asIntN(64, (hi << 32n) + lo);
  }
}
