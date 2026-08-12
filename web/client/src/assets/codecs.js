/*
 * The codec pair every asset decoder in this directory expects, wired to the dependency-free
 * implementations next door.
 *
 * The decoders take `{ inflate, decodePNG }` as an injected option so they stay usable from
 * Node tooling with `node:zlib` (faster, and how the spike ran). This module is the default
 * for anything with no platform to lean on — i.e. the browser client.
 */
import { gunzip } from './inflate.js';
import { decodePNG } from './png.js';

/** `inflate` is gzip here: every compressed blob in the game's assets is a Java GZIP stream. */
export const codecs = { inflate: gunzip, decodePNG };
