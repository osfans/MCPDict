#!/usr/bin/env python3

import json, os, re
from openpyxl import load_workbook
from opencc import OpenCC

curdir = os.path.dirname(__file__)
spath = os.path.join(curdir, "..", "漢字音典字表檔案（長期更新）.xlsx")
tpath = os.path.join(curdir, "output", os.path.basename(__file__).replace(".py", ".json"))

FeatureCollection = {
  "type": "FeatureCollection",
  "features": []
}

n2o_dict = {}

for line in open("tables/data/mulcodechar.dt", encoding="U8"):
	if not line or line[0] == "#": continue
	fs = line.strip().split("-")
	if len(fs) < 2: continue
	n2o_dict[fs[0]] = fs[1]

opencc_s2t = OpenCC("s2t.json")
opencc_t2s = OpenCC("t2s.json")

def s2t(s):
	if not s: return ""
	if type(s) is not str: return s
	s = opencc_s2t.convert(s)\
		.replace("樑","梁")\
		.replace("嶽","岳")\
		.replace("慄", "栗")
	for n, o in n2o_dict.items():
		s = s.replace(n, o)
	return s

def n2o(s):
	if not s: return ""
	for n, o in n2o_dict.items():
		s = s.replace(n, o)
	return s

def o2n(s):
	if not s: return ""
	for n, o in n2o_dict.items():
		s = s.replace(o, n)
	return s

def t2s(s, prepare = True):
	s = o2n(s)
	if prepare:
		return s
	return opencc_t2s.convert(s)

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
	l = dict()
	t4s = [""] * 6
	for i,ts in enumerate(tones):
		i = i + 1
		index = i
		if not ts: continue
		ts = ts.lower()
		for j,t in enumerate(ts.split(",")):
			if t.startswith("["):
				index = t[1:t.index("]")]
				t = t[t.index("]")+1:]
			if t[0].isdigit() or t[0] == "-":
				n = t.lstrip("012345-")
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
			l[str(index)] = (v,str(t8),str(t4),n,m)
	return json.dumps(l, ensure_ascii=False).lower()

def load():
	if not outdated():
		return json.load(open(tpath,encoding="U8"))
	d = dict()
	wb = load_workbook(spath)
	sheet = wb.worksheets[0]
	firstLine = True
	fields = []
	for row in sheet.rows:
		line = [j.value if j.value else "" for i,j in enumerate(row)]
		if firstLine:
			fields = line
			firstLine = False
		fs = dict(zip(fields, row))
		ver = fs["版本/更新時間"].value
		if type(ver) is str:
			if ver == "/": ver = None
			else: continue
		else:
			ver = ver.strftime("%Y-%m-%d") if ver else None
		lang = n2o(fs["語言"].value)
		short = n2o(fs["簡稱"].value)
		filename = fs["文件名"].value
		if not filename or filename.startswith("#"): continue
		fileformat = fs["字表格式"].value
		fileskip = int(fs["跳過行數"].value) if fs["跳過行數"].value else 0
		orders = [fs[i].value for i in ("地圖集二排序", "音典排序","陳邡排序")]
		colors = [fs[i].fill.fgColor.value[2:] for i in ("地圖集二顏色", "音典顏色","陳邡顏色")]
		subcolors = [fs[i].fill.fgColor.value[2:] for i in ("地圖集二顏色", "音典過渡色","陳邡顏色")]
		types = [fs[i].value for i in ("地圖集二分區", "音典分區","下拉2，折疊分区")]
		tmp = types[0]
		if tmp:
			types[0] += "," + (tmp.split("-")[0] if "-" in tmp else "")
		else: types[0] = ","
		tmp = types[1]
		if tmp:
			types[1] += "," + (tmp.split("-")[1] if "-" in tmp else "")
		else: types[1] = ","
		start = fields.index("下拉1")
		collapse = fs["下拉2，折疊分区"].value
		if collapse == None: collapse = ""
		dropdown = [row[i].value if row[i].value else "" for i in range(start, start + 6)]
		if types[2] == None: types[2] = ""
		types[2] = collapse + "," + (",".join(dropdown))
		point = fs["經緯度"].value
		if point: point = point.replace(" ", "").replace("，",",").strip()
		places = [fs[i].value if fs[i].value else "" for i in ("省/自治區/直轄市","地區/市/州","縣/市/區","鄕/鎭/街道","村/社區/居民點")]
		place = ("".join(places)).replace("/", "")
		island = fs["方言島"].value
		size = fs["級別(5星爲代表方言-1星最大時顯示)"].value
		size = size.count("★") if size else 0
		j = fields.index("[1]陰平")
		tones = [line[i] for i in range(j,j+10)]
		editor = fs["錄入人"].value
		books = fs["來源"]
		book = None
		if books.value:
			if books.hyperlink:
				target = books.hyperlink.target
				book = f"<a href={target}>{books.value}</a>"
			else:
				book = books.value
		note = fs["說明"].value
		jf = fs["繁簡"].value
		for i,c in enumerate(subcolors):
			if c and c != "000000" and c != colors[i]:
				colors[i] += f",{c}"
		colors = [re.sub(r"(\w+)", "#\\1", i) for i in colors]
		marker_size = "small"
		if size >= 4: marker_size = "large"
		elif size == 3: marker_size = "medium"
		if not editor or editor == "Web":
			editor = ""
		d[short] = {
			"序號":row[0].row,
			"語言":lang,
			"簡稱":short,
			"文件名":filename,
			"文件格式":fileformat,
			"跳過行數":fileskip,
			"方言島": island == "☑",
			"地圖集二排序":orders[0],
			"地圖集二顏色":colors[0],
			"地圖集二分區":types[0],
			"音典排序":orders[1],
			"音典顏色":colors[1],
			"音典分區":types[1],
			"陳邡排序":orders[2],
			"陳邡顏色":colors[2],
			"陳邡分區":s2t(types[2]),
			"省":s2t(places[0]).strip("*"),
			"市":places[1],
			"縣":places[2],
			"鎮":places[3],
			"村":places[4],
			"地點": place,
			"版本":ver,
			"經緯度":point,
			"級別":str(size),
			"錄入人":editor,
			"參考資料":book,
			"說明":note,
			"簡繁":jf,
			"聲調":getTones(tones),
		}
		if not point: continue
		jd, wd = map(float, point.split(","))
		if abs(wd) > 90:
			jd, wd = wd, jd
		point = f"{jd},{wd}"
		d[short]["經緯度"] = point
		Feature = {
			"type": "Feature",
			"properties": {
				"語言": lang,
				"地點": place,
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
	json.dump(FeatureCollection, fp=open("../方言.geojson","w",encoding="U8",newline="\n"),ensure_ascii=False,indent=2)
	json.dump(d, fp=open(tpath,"w",encoding="U8",newline="\n"),ensure_ascii=False,indent=2)
	return d

