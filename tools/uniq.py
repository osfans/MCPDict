#!/usr/bin/env python3
import sys

if len(sys.argv) != 3:
	print(f"Usage: {sys.argv[0]} input output")
	sys.exit(1)
input, output = sys.argv[1:3]

with open(input, "r", encoding="utf-8") as infile, open(output, "w", encoding="utf-8") as outfile:
    seen = set()
    for line in infile:
        if line not in seen:
            outfile.write(line)
            seen.add(line)