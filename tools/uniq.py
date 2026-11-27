#!/usr/bin/env python3
import sys

if len(sys.argv) != 2:
	print(f"Usage: {sys.argv[0]} input output")
	sys.exit(1)
input = sys.argv[1]

lines = list()
with open(input, "r", encoding="utf-8") as infile:
    seen = set()
    for line in infile:
        if line not in seen:
            lines.append(line)
            seen.add(line)
open(input, "w", encoding="utf-8").writelines(lines)
