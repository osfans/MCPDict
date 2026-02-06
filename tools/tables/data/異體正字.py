import re, sys

sim = set("与")
l = set()
for line in open("IDS.txt", encoding="utf-8"):
	fs = line.split("\t")
	if len(fs) < 3: continue
	hz = fs[1]
	ids = "".join(fs[2:])
	if len(set(ids).intersection(sim)) > 0:
		l.add(hz)

linez = set()
for line in open("正字.tsv", encoding="U8"):
	line = line.strip()
	if " " not in line:
		linez.add(line)

lines2 = list()
lines = list()
for line in open("異體字.tsv", encoding="U8"):
	line = line.strip()
	if not line: continue
	a, b = line.split("\t")
	if len(a) == 1 and len(b) == 1 and a in l:
		lines2.append(line + "\n")
	else:
		if line not in linez:
			lines.append(line + "\n")

open("異體字.tsv", "w", encoding="U8").writelines(lines)
if lines2:
	open("正字2.tsv", "w", encoding="U8").writelines(lines2)