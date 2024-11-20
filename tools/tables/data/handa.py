d = dict()
for line in open("handa.txt", encoding="U8"):
    line = line.strip()
    fs = line.split("\t")
    hz, yt = fs[0], fs[-1]
    if len(hz) == 1 and len(yt) == 1:
        d[hz] = yt

lines = []
f = open("正字.tsv", encoding="U8")
for line in f:
    lines.append(line)
    hz = line.split("\t")[0]
    if hz in d:
        yt = d.pop(hz)
        if yt not in line:
            print(hz, yt)
f.close()

for hz, yt in d.items():
    lines.append(f"{hz}\t{yt}\n")

f = open("正字.tsv", "w", encoding="U8")
f.writelines(lines)
f.close()
