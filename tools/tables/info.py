#!/usr/bin/env python3

import json, os
from openpyxl import load_workbook
from opencc import OpenCC

spath = "汉音字典字表档案（长期更新）.xlsx"
tpath = "tables/output/info.tsv"

FeatureCollection = {
  "type": "FeatureCollection",
  "features": []
}

opencc = OpenCC("s2t.json")

def convert(s):
	if not s: return ""
	if type(s) is not str: return s
	return opencc.convert(s).replace("清","淸")\
							.replace("榆","楡")\
							.replace("樑","梁")\
							.replace("嶽","岳")\
							.replace("慄", "栗")

def outdated():
	if not os.path.exists(tpath): return True
	classtime = os.path.getmtime(__file__)
	stime = os.path.getmtime(spath)
	if classtime > stime: stime = classtime
	ttime = os.path.getmtime(tpath)
	return stime > ttime

markers = {1: '꜀', 2: '꜁', 3: '꜂', 4: '꜃', 5: '꜄', 6: '꜅', 7: '꜆', 8: '꜇'}
markers2 = {1: '꜆', 2: '꜆', 3: '꜄', 4: '꜅', 5: '꜂', 6: '꜃', 7: '꜀', 8: '꜁'}
def getTones(tones):
	l = [""] * 40
	t4s = [""] * 6
	for i,ts in enumerate(tones):
		i = i + 1
		index = i
		if ts:
			for j,t in enumerate(ts.split(",")):
				if t.startswith("["):
					index = t[1:t.index("]")]
					if index[-1] in "abcd": index = int(index[:-1]) + 10 *(ord(index[-1])-ord("a"))
					else: index = int(index)
					t = t[t.index("]")+1:]
				if t[0].isdigit():
					n = t.lstrip("012345")
					v = t[:len(t)-len(n)]
				else:
					n = t
					v = ""
				#334 1 1a 陰平 ꜀
				t8 = i
				t4 = (i+1)//2
				if i == 10:
					t8 = 0
					t4 = 0
				elif i == 9: t4 = 5
				if "," in ts:
					t8 = str(t8) + chr(ord("a") + j)
				if "," in ts or tones[i - 2 if i % 2 == 0 else i]:
					t4s[t4]+="1"
					t4 = str(t4) + chr(ord("a") + len(t4s[t4]) - 1)
				m = markers.get(i, '') if j == 0 else markers2.get(i, '')
				l[index] = f"{v} {t8} {t4} {n} {m}"
	return ",".join(l[1:]).rstrip(",")

def getInfos():
	d = dict()
	if not outdated():
		f = open(tpath,encoding="U8")
		for line in f:
			fs = line.rstrip("\n").split("\t")
			d[fs[0]] = fs[1:]
		f.close()
		return d
	sheets = load_workbook(spath)
	sheet = sheets["档案"]
	for row in sheet.rows:
		fs = [convert(row[i].value) for i in range(37)]
		if not fs[4] or type(fs[4]) is str: continue
		lang = fs[0].strip()
		name = fs[1].strip()
		color = row[3].fill.fgColor.value[2:]
		subcolor = row[2].fill.fgColor.value[2:]
		ver = fs[4].strftime("%Y-%m-%d")
		point = fs[5].replace(" ", "").replace("，",",").strip()
		place = "".join(fs[6:11])
		island = fs[11]
		size = fs[12].count("★")
		editor = fs[14].strip("/")
		book = fs[15].strip("/")
		note = fs[16]
		tones = fs[19:29]
		section = fs[36]
		jf = fs[17]
		if book:
			if row[15].hyperlink:
				target = row[15].hyperlink.target
				book = f"<a herf={target}>{book}</a>"

		colors = [color]
		if subcolor != "000000":
			colors.append(subcolor)
		colors = ["#"+ i for i in colors]
		color = ",".join(colors)
		marker_size = "small"
		if size >= 4: marker_size = "large"
		elif size == 3: marker_size = "medium"
		if not editor or editor == "Web":
			editor = ""
		d[name] = lang, color, ver, point, str(size), editor, book, note, jf, getTones(tones)
		if not point: continue
		wd, jd = map(float, point.split(","))
		Feature = {
			"type": "Feature",
			"properties": {
				"方言": name,
				"地點": place,
				"方言片": section,
				"marker-size": marker_size,
				"marker-color": colors[0],
				#"marker-symbol": label,
			},
			"geometry": {
				"type": "Point",
				"coordinates": [jd, wd]
			}
		}
		if island == "☑":
			Feature["properties"]["方言島"] = island
		if ver:
			Feature["properties"]["版本"] = ver
		if editor:
			Feature["properties"]["錄入人"] = editor
		if book:
			Feature["properties"]["參考資料"] = book
		if jf:
			Feature["properties"]["繁簡"] = jf
		FeatureCollection["features"].append(Feature)
	json.dump(FeatureCollection, fp=open("../方言.geojson","w",encoding="U8"),ensure_ascii=False,indent=2)
	f = open(tpath, "w",encoding="U8")
	for i in d:
		print("%s\t%s" % (i, "\t".join(map(lambda x:x if x else "", d[i]))), file=f)
	f.close()
	return d

