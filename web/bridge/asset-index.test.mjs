/*
 * What /data/ will and will not hand out.
 *
 * The data directory is the server's, not a public asset bundle: quest scripts, drop tables,
 * NPC spawns and per-map collision all live in it. These tests pin the two things that keep
 * them out of a browser — the containment check and the allow-list — because a regression in
 * either is invisible until someone looks.
 */
import test from 'node:test';
import assert from 'node:assert/strict';
import path from 'node:path';
import { isAllowedDataPath, resolveDataFile } from './asset-index.js';

const ROOT = path.resolve('/srv/data');

test('the client asset trees are reachable', () => {
  for (const rel of [
    'Areas/87_1/client.pkg',
    'Areas/87_1/client_l.pkg',
    'Areas/1/client.pkg',
    'client_pkg/Flash/male.ctn',
    'client_pkg/Flash/body1.pip',
    'client_res/240x320/ui_res.pip',
    // compiled VM script images: the client downloads these to boot, exactly
    // like the 2011 Java client did from the server
    'scripts/Flash/game_init_Flash.etf.gz',
    'scripts/Flash/game_world.etf.gz',
    'scripts/240x320/lib_builtin_240x320.etf.gz',
  ]) {
    assert.equal(isAllowedDataPath(rel), true, rel);
  }
});

test('server-side game data is not', () => {
  for (const rel of [
    'Areas/87_1/info.xml',        // NPC spawns, exits, triggers
    'Areas/87_1/game.map',        // collision
    'Areas/87_1/game_l.map',
    'Areas',
    'Areas/87_1',
    'scripts/Flash/1.txt',        // the quest VM sources
    'scripts/Flash/game_init.etf',        // not the compiled .gz image
    'scripts/Flash/game_init.etf.gz.txt', // not a script image either
    'scripts/Flash/sub/dir.etf.gz',       // scripts sit flat in the model dir
    'scripts/game_init_Flash.etf.gz',     // no model directory
    'Quests/1732.xml',
    'NPCTemplates/1.xml',
    'PathFinder/1395.pth',
    'npc.xml',
    'config.xml',
    'client_pkg.xml',             // the asset map: derive from it, do not publish it
    'client_pkg',
    'client_res',
  ]) {
    assert.equal(isAllowedDataPath(rel), false, rel);
  }
});

test('dot segments, hidden files and CVS metadata are refused', () => {
  for (const rel of [
    '../secret',
    'client_pkg/../../secret',
    'client_pkg/./Flash/male.ctn',
    'client_pkg/.git/config',
    'client_pkg/CVS/Entries',
    'client_res/CVS/Root',
    'client_pkg//Flash/male.ctn',
    'client_pkg\\Flash\\male.ctn',
    'client_pkg/Flash/male.ctn\0.txt',
    '',
    '/',
  ]) {
    assert.equal(isAllowedDataPath(rel), false, JSON.stringify(rel));
  }
});

test('resolveDataFile keeps traversal inside the data directory', () => {
  for (const rel of [
    '/../../../etc/passwd',
    '/Areas/../../etc/passwd',
    '/Areas/87_1/../../../../etc/passwd',
    '/..%2f..%2fetc/passwd',        // already decoded by the caller; stays a name, not a path
    '/',
    '/Areas/87_1/info.xml',
    '/scripts/Flash/1.txt',
    '/scripts/Flash/npc.xml',
  ]) {
    assert.equal(resolveDataFile(ROOT, rel), null, rel);
  }
});

test('resolveDataFile returns a real path under the root for allowed files', () => {
  const file = resolveDataFile(ROOT, '/Areas/87_1/client.pkg');
  assert.equal(file, path.join(ROOT, 'Areas', '87_1', 'client.pkg'));
  assert.equal(file.startsWith(ROOT + path.sep), true);

  // a leading slash is optional — the caller strips /data
  assert.equal(resolveDataFile(ROOT, 'client_pkg/Flash/male.ctn'),
    path.join(ROOT, 'client_pkg', 'Flash', 'male.ctn'));

  const script = resolveDataFile(ROOT, '/scripts/Flash/game_init_Flash.etf.gz');
  assert.equal(script, path.join(ROOT, 'scripts', 'Flash', 'game_init_Flash.etf.gz'));
});
