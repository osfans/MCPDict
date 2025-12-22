import os, sys
d = dict()
f = open("../app/src/main/assets/opencc/STCharacters.txt", encoding="U8")
for line in f:
	line = line.strip()
	if not line: continue
	if " " in line: continue
	if "\t" not in line: continue
	if line.startswith("#"): continue
	if line[0] in "荆从咏听卺懔凭肴昵床鉤袜虱肮猬椁况达猎却疴笋榝刹霉叠册脉霡儿幂担咤洼厨苧痒溆恒沄炖雇窑么篱机气离莅栖洒凉": continue
	k, v = line.split("\t")
	d[k] = v
a = str.maketrans(d)

name = sys.argv[1]
lines = []
f = open(name, "r", encoding="U8")
for line in f:
	line = line.translate(a)
	lines.append(line)
f.close()
f = open(name, "w", newline="\n", encoding="U8")
f.writelines(lines)
f.close()