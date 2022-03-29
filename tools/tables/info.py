#!/usr/bin/env python3

import subprocess, json, os
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
							.replace("嶽","岳")

def outdated():
	if not os.path.exists(tpath): return True
	classtime = os.path.getmtime(__file__)
	stime = os.path.getmtime(spath)
	if classtime > stime: stime = classtime
	ttime = os.path.getmtime(tpath)
	return stime > ttime

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
		fs = [convert(row[i].value) for i in range(26)]
		if not fs[3] or type(fs[3]) is str: continue
		lang = fs[0].strip()
		name = fs[1].strip()
		if lang == name:
			lang = ""
		color = row[1].fill.fgColor.value[2:]
		subcolor = row[2].fill.fgColor.value[2:]
		ver = fs[3].strftime("%Y-%m-%d")
		point = fs[4].replace(" ", "").replace("，",",").strip()
		place = "".join(fs[5:10])
		island = fs[10]
		size = fs[11].count("★")
		editor = fs[13].strip("/")
		book = fs[14].strip("/")
		section = fs[23]
		jf = fs[15]
		#label = fs[5][0].lower()

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
		if book:
			if row[14].hyperlink:
				target = row[14].hyperlink.target
				book = f"<a herf={target}>{book}</a>"
		d[name] = lang, color, ver, point, str(size), editor, book
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

