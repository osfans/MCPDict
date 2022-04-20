#!/usr/bin/env python3

import json, os, re
from openpyxl import load_workbook
from opencc import OpenCC

spath = "漢字音典字表檔案（長期更新）.xlsx"
tpath = "tables/output/%s.json" % (__name__.split(".")[-1])

FeatureCollection = {
  "type": "FeatureCollection",
  "features": []
}

opencc = OpenCC("s2t.json")

def convert(s):
	if not s: return ""
	if type(s) is not str: return s
	return opencc.convert(s)\
		.replace("清","淸")\
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
		if not ts: continue
		ts = ts.lower()
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

def load():
	if not outdated():
		return json.load(open(tpath,encoding="U8"))
	d = dict()
	wb = load_workbook(spath)
	sheet = wb.worksheets[0]
	for row in sheet.rows:
		fs = [j.value if j.value else "" for i,j in enumerate(row)]
		ver = fs[5]
		if type(ver) is str and ver: continue
		ver = ver.strftime("%Y-%m-%d") if ver else None
		lang = fs[0].strip()
		short = fs[1].strip()
		filename = fs[2].strip()
		if not filename: continue
		fileformat = fs[3].strip()
		fileskip = int(fs[4]) if fs[4] else 0
		orders = [str(fs[i]).strip() for i in (30,33,38,37)]
		colors = [row[i].fill.fgColor.value[2:] for i in (31,33,39)]
		subcolors = [row[i].fill.fgColor.value[2:] for i in (31,34,40)]
		types = [fs[i].strip() for i in (32,36)]
		types.append(convert(fs[49]))
		types.append(",".join(fs[41:49]))
		j = 6
		point = fs[j].replace(" ", "").replace("，",",").strip()
		places = fs[j+1:j+6]
		island = fs[j+6]
		size = fs[j+7].count("★")
		tones = fs[j+8:j+18]
		en = 24
		editor = fs[en].strip("/")
		book = fs[en+1].strip("/")
		if book:
			if row[en+1].hyperlink:
				target = row[en+1].hyperlink.target
				book = f"<a herf={target}>{book}</a>"
		note = fs[en+2]
		jf = convert(fs[en+3])
		for i,c in enumerate(subcolors):
			if c and c != "000000" and c != colors[i]:
				colors[i] += f",{c}"
		colors = [re.sub("(\w+)", "#\\1", i) for i in colors]
		marker_size = "small"
		if size >= 4: marker_size = "large"
		elif size == 3: marker_size = "medium"
		if not editor or editor == "Web":
			editor = ""
		d[lang] = {
			"語言":lang,
			"簡稱":short,
			"文件名":filename,
			"文件格式":fileformat,
			"跳過行數":fileskip,
			"地圖集二排序":orders[0],
			"地圖集二顏色":colors[0],
			"地圖集二分區":types[0],
			"音典排序":orders[1],
			"音典顏色":colors[1],
			"音典分區":types[1],
			"陳邡排序":orders[2],
			"陳邡顏色":colors[2],
			"陳邡分區":types[2],
			"陳邡二排序":orders[3],
			"陳邡二顏色":colors[2],
			"陳邡二分區":types[3],
			"省":convert(places[0]).strip("*"),
			"市":places[1],
			"縣":places[2],
			"鎮":places[3],
			"村":places[4],
			"版本":ver,
			"坐標":point,
			"級別":str(size),
			"錄入人":editor,
			"參考資料":book,
			"說明":note,
			"簡繁":jf,
			"聲調":getTones(tones),
		}
		try:
			wd, jd = map(float, point.split(","))
		except:
			continue
		Feature = {
			"type": "Feature",
			"properties": {
				"語言": lang,
				"地點": "".join(places),
				"地圖集二分區": types[0],
				"音典分區": types[1],
				"陳邡分區":types[2],
				"marker-color": colors[0],
				"marker-size": marker_size,
				"marker-symbol": orders[0][0].upper() if orders[0] else "",
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
	json.dump(d, fp=open(tpath,"w",encoding="U8"),ensure_ascii=False,indent=2)
	return d

