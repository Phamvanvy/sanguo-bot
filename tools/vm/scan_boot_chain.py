"""Find who loads game_world / game_panel in the Java client."""
import os
import re

root = r'Game/sangobuildVn/client/src'
pat = re.compile(r'loadVMGame|"game_world"|"game_panel"|"game_init"')
for dirpath, dirs, files in os.walk(root):
    for f in files:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dirpath, f)
        src = open(p, 'rb').read().decode('latin1')
        for i, line in enumerate(src.split(chr(10))):
            if pat.search(line) and not line.strip().startswith('//'):
                print(f'{os.path.relpath(p, root)}:{i+1}: {line.strip()[:110]}')