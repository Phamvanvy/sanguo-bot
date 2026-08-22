"""Print the VM.java source for given syscall ids."""
import re
import sys

SRC = r'Game/sangobuildVn/client/src/com/pip/ui/VM.java'
ids = [int(a, 16) for a in sys.argv[1:]]
text = open(SRC, 'rb').read().decode('latin1')
lines = text.split(chr(10))
for i, line in enumerate(lines):
    m = re.search(r'case\s+0x([0-9a-fA-F]+)', line)
    if m and int(m.group(1), 16) in ids:
        print(f'--- {line.strip()}   (line {i+1})')
        for j in range(i + 1, min(i + 12, len(lines))):
            print('   ', lines[j].rstrip()[:120])
        print()