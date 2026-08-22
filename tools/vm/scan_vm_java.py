"""Scan VM.java for structure: method boundaries and syscall switch ranges."""
import re
import sys

path = sys.argv[1] if len(sys.argv) > 1 else 'Game/sangobuildVn/client/src/com/pip/ui/VM.java'
src = open(path, 'rb').read().decode('latin1')
lines = src.split(chr(10))
print('total lines:', len(lines))

for i, l in enumerate(lines):
    s = l.strip()
    if re.match(r'(public|private|protected|static).*\b(syscall|execute|processInst|callback)\w*\s*\(', s) \
            or 'switch(funcID)' in s or 'switch (funcID)' in s:
        print(f'{i+1:5d}  {s[:100]}')