#!/usr/bin/env python3

import subprocess, json, os
from openpyxl import load_workbook

spath = "汉音字典字表档案（长期更新）.xlsx"
tpath = "tables/output/info.tsv"

FeatureCollection = {
  "type": "FeatureCollection",
  "features": []
}

def opencc(sim):
	return subprocess.check_output(["opencc"], input=sim, text=True)

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
		f = open(tpath)
		for line in f:
			fs = line.rstrip("\n").split("\t")
			d[fs[0]] = fs[1:]
		f.close()
		return d
	sheets = load_workbook(spath)
	sheet = sheets["档案"]
	for row in sheet.rows:
		fs = [row[i].value if row[i].value else "" for i in range(26)]
		if not fs[2] or type(fs[2]) is str: continue
		color = row[9].fill.fgColor.value[2:]
		colors = [color]
		subcolor = row[10].fill.fgColor.value[2:]
		if subcolor != "000000":
			colors.append(subcolor)
		colors = ["#"+ i for i in colors]
		color = ",".join(colors)
		point = fs[11].replace(" ", "")
		wd, jd = map(float, point.split(","))
		place = "".join(fs[12:17])
		size = fs[18].count("★")
		marker_size = "small"
		if size >= 4: marker_size = "large"
		elif size == 3: marker_size = "medium"
		if fs[22] and fs[22] != "Web":
			editor = opencc(fs[22])
		book = ""
		if fs[23]:
			book = opencc(fs[23])
			if row[23].hyperlink:
				target = row[23].hyperlink.target
				book = f"<a herf={target}>{book}</a>"
		name = fs[0] if "〃" in fs[1] else fs[1]
		name = opencc(name).replace("清","淸").replace("榆","楡").replace("峯","峰").replace("樑","梁")
		section = opencc("".join(fs[6]))
		label = fs[5][0].lower()
		ver = None
		ver = fs[2].strftime("%Y-%m-%d")
		editor = None
		d[name] = color, ver, point, str(size)
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
		if fs[17] == "☑":
			Feature["properties"]["方言島"] = fs[17]
		if ver:
			Feature["properties"]["版本"] = ver
		if editor:
			Feature["properties"]["錄入人"] = editor
		if book:
			Feature["properties"]["參考文獻"] = book
		if fs[24]:
			Feature["properties"]["繁簡"] = fs[24]
		FeatureCollection["features"].append(Feature)
	json.dump(FeatureCollection, fp=open("../方言.geojson","w"),ensure_ascii=False,indent=2)
	f = open(tpath, "w")
	for i in d:
		print("%s\t%s" % (i, "\t".join(map(lambda x:x if x else "", d[i]))), file=f)
	f.close()
	return d

