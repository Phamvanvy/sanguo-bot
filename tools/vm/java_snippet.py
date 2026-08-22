"""Print a line range of a Java source file (latin-1)."""
import sys

path, start, end = sys.argv[1], int(sys.argv[2]), int(sys.argv[3])
lines = open(path, 'rb').read().decode('latin1').split(chr(10))
for i in range(start - 1, min(end, len(lines))):
    print(f'{i+1}: {lines[i]}')