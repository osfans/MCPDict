import re, sys

if len(sys.argv) < 2:
	print("Usage: python ids.py file")
	sys.exit(1)
fnames = sys.argv[1:]
d = {}
for line in open("IDS.txt", encoding="utf-8"):
	fs = line.split("\t")
	if len(fs) < 3: continue
	hz = fs[1]
	ids = [i for i in fs[2:] if not i.startswith("*")]
	for id in ids:
		id = id.split("(")[0].strip(" ^$")
		d[id] = hz

def rep(m):
	g = m.group(0)
	for i in range(7, 2, -1):
		p = g[:i]
		if p in d:
			g = g.replace(p, d[p])
			continue
	return d.get(m.group(0), g)

for fname in fnames:
	lines = []
	f = open(fname, encoding="utf-8")
	for line in f:
		line = re.sub("[⿰⿱⿲⿳⿴⿵⿶⿷⿸⿹⿺⿻⿼⿽㇯⿾⿿〾].*$", rep, line)
		lines.append(line)
	f.close()

	f = open(fname, "w", encoding="utf-8")
	f.writelines(lines)
	f.close()