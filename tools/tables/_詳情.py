#!/usr/bin/env python3

import json, os, re
from openpyxl import load_workbook
from .__init__ import n2o, s2t

curdir = os.path.dirname(__file__)
spath = "漢字音典字表檔案（長期更新）.xlsx"
if not os.path.exists(spath):
	spath = os.path.join(curdir, "..", spath)
tpath = os.path.join(curdir, "output", os.path.basename(__file__).rstrip("c").replace(".py", ".json"))

FeatureCollection = {
	"type": "FeatureCollection",
	"features": []
}

def 過時():
	if not os.path.exists(tpath): return True
	classtime = os.path.getmtime(__file__.rstrip("c"))
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
		ts = str(ts).lower().replace("ˀ", "6").replace("ʔ", "0").replace("，", ",").replace(" ", "")
		tsn = ts.count(",") + 1
		for j,t in enumerate(ts.split(",")):
			if t.startswith("["):
				index = t[1:t.index("]")]
				t = t[t.index("]")+1:]
			elif t and tsn > 1:
				index2 = chr(ord('a') + j)
				index = f"{i}{index2}"
			if t[0].isdigit() or t[0] == "-":
				n = t.lstrip("0123456-/")
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

def normNames(s):
	if not s: return ""
	if type(s) is float: s = str(int(s))
	if type(s) is not str: s = str(s)
	s = re.sub(r"([（\(])", " \\1", s)
	return re.sub(" ?[、，,&] ?", ",", s)

def normLangName(s):
	return n2o(s2t(s.strip()))

def normJW(s):
	if s:
		s = s.replace(" ", "").replace("，",",").strip()
		jd, wd = map(float, s.split(","))
		s = f"{jd:.6f},{wd:.6f}"
	return s

def getMarkerSize(size):
	if size >= 4: return "large"
	if size == 3: return "medium"
	return "small"

def normVer(s):
	if type(s) is str:
		if s == "/": s = None
	else:
		s = s.strftime("%Y-%m-%d") if s else None
	return s

def normSource(books):
	if books.value:
		if books.hyperlink:
			target = books.hyperlink.target
			return f"<a href={target}>{books.value}</a>"
		else:
			return books.value
	return None

def 加載(省=None):
	if not 省 and not 過時():
		return json.load(open(tpath,encoding="U8"))
	d = dict()
	wb = load_workbook(spath)
	sheet = wb.worksheets[0]
	lineCount = 0
	fields = []
	# 音典顔色分區 = dict()
	for row in sheet.rows:
		lineCount += 1
		行 = [j.value if j.value else "" for j in row]
		if lineCount == 1: fields = 行
		if lineCount <= 2:
			continue
		列 = dict(zip(fields, 行))
		語言 = normLangName(列["語言"])
		簡稱 = normLangName(列["簡稱"]).replace("-", "－").replace("(", "（").replace(")", "）")
		文件名 = 列["文件名"]
		if 列["是否有人在做"].strip() not in ("已做", "重做"):
			continue
		if not 文件名 or 文件名.startswith("#"):
			print(f"{語言} 沒有字表文件: {文件名}")
			continue
		if 簡稱 in d:
			print(f"{語言} 的簡稱 {簡稱} 重複")
			continue
		音系說明 = 列["音系"]
		說明 = 列["說明"]
		繁簡 = 列["繁簡"]
		字聲韻調註列名 = 列["字聲韻調註列名"].upper().replace("[", ",").replace("]", ",")
		字表格式 = 列["字表格式"]
		經緯度 = normJW(列["經緯度"])
		方言島 = 列["方言島"] == "☑"
		作者 = normNames(列["作者"])
		錄入人 = normNames(列["錄入人"])
		維護人 = normNames(列["維護人"])
		推薦人 = normNames(列["推薦人"])
		網站 = "" if 列["在線查詢"].startswith("#") else 列["在線查詢"]
		網址 = 列["網址"]
		字表來源 = normSource(row[fields.index("字表來源（母本）")])
		參考文獻 = 列["參考文獻（母本補充材料）"]
		補充閲讀 = 列["補充閲讀"]
		版本 = normVer(列["版本/更新時間"])
		跳過行數 = int(列["跳過行數"]) if 列["跳過行數"] else 0
		字表使用調值 = 列["字表使用調值"] == "☑"
		地圖級別 = 列["地圖級別"].count("★") if 列["地圖級別"] else 0

		j = fields.index("[1]陰平")
		聲調 = getTones([列[fields[i]] for i in range(j, j+10)])

		types = [s2t(列[i]).replace("-", "－") for i in ("地圖集二分區", "音典分區", "下拉1，折疊分区")]
		if types[2] and 列["下拉2"]: types[2] += "," + 列["下拉2"]

		orders = [列[i].strip() for i in ("地圖集二排序", "音典排序", "陳邡排序")]
		colors = [row[fields.index(i)].fill.fgColor.value[2:] for i in ("地圖集二顏色", "音典顏色","陳邡顏色")]
		# 音典顔色分區[colors[1]] = types[1]
		subfgColor = row[fields.index("音典過渡色")].fill.fgColor
		if subfgColor.type == "rgb":
			subcolor = subfgColor.rgb[2:]
			if subcolor and subcolor != "000000" and subcolor != "FFFFFF" and subcolor != colors[1]:
					colors[1] += f",{subcolor}"
			else:
				subcolor = ""
		colors = [re.sub(r"(\w+)", "#\\1", i) for i in colors]

		places = [s2t(列[i]) if 列[i] else "" for i in ("省/自治區/直轄市","地區/市/州","縣/市/區","鄕/鎭/街道","村/社區/居民點","自然村")]
		if 簡稱 == "普通話" and 省:
			places = ["", "", "", "", ""]
		elif 省 and places[0] and places[0] not in 省:
			print(f"{語言} 省份 {places[0]} 不在指定省份 {省}")
			continue
		地點 = ("".join(places)).replace("/", "")
		行政區級別 = 列["行政區級別"]
		if not 行政區級別:
			行政區級別 = "省會,地級" if 列["省會"] == "☑" else ""
		if not 行政區級別:
			n = 6 - places.count("")
			if n == 1:
				行政區級別 = "省級"
			elif n == 2:
				行政區級別 = "地級"
			elif n == 3:
				行政區級別 = "縣級"
			elif n == 4:
				行政區級別 = "鄕級"
			elif n == 5:
				行政區級別 = "村級"
			elif n == 6:
				行政區級別 = "自然村級"
		d[簡稱] = {
			"語言":語言,
			"簡稱":簡稱,
			"版本":版本,
			"文件名":文件名,
			"字表格式":字表格式,
			"跳過行數":跳過行數,
			"字表使用調值": 字表使用調值,
			"字聲韻調註列名":字聲韻調註列名,
			"網站":網站,
			"網址":網址,
			"方言島": 方言島,
			"地圖集二排序":orders[0],
			"地圖集二顏色":colors[0],
			"地圖集二分區":types[0],
			"音典排序":orders[1],
			"音典顏色":colors[1],
			"音典分區":types[1],
			"陳邡排序":orders[2],
			"陳邡顏色":colors[2],
			"陳邡分區":types[2],
			"行政區級別": 行政區級別,
			"省":places[0].strip("*"),
			"市":places[1],
			"縣":places[2],
			"鎮":places[3],
			"村":places[4],
			"自然村":places[5],
			"地點": 地點,
			"經緯度":經緯度,
			"地圖級別":str(地圖級別),
			"作者":作者,
			"錄入人":錄入人,
			"維護人":維護人,
			"推薦人":推薦人,
			"字表來源": 字表來源,
			"參考文獻":參考文獻,
			"補充閲讀":補充閲讀,
			"音系說明":音系說明,
			"說明":說明,
			"繁簡":繁簡,
			"聲調":聲調
		}
		if not 經緯度: continue
		Feature = {
			"type": "Feature",
			"properties": {
				"marker-color": colors[0],
				"marker-size": getMarkerSize(地圖級別),
				"marker-symbol": orders[0][0].lower() if orders[0] else "",
				"title": 簡稱,
			},
			"geometry": {
				"type": "Point",
				"coordinates": eval(f"[{經緯度}]")
			}
		}
		for i in ["語言", "地點", "地圖集二分區", "音典分區", "陳邡分區", '方言島', '版本', '作者', '錄入人', '維護人', '字表來源', '參考文獻', '補充閲讀']:
			if d[簡稱][i]:
				Feature["properties"][i] = d[簡稱][i]
		FeatureCollection["features"].append(Feature)
	# for k, v in d.items():
	# 	if "," in v["音典顏色"]:
	# 		subcolor = v["音典顏色"].split(",")[-1][1:]
	# 		if subcolor in 音典顔色分區:
	# 			v["音典分區"] += "," + 音典顔色分區[subcolor]
	# 		else:
	# 			print(k, "音典過渡色無對應分區")
	geojsonpath = os.path.join(curdir, "../..", "方言.geojson")
	if os.path.exists(geojsonpath):
		json.dump(FeatureCollection, fp=open(geojsonpath, "w",encoding="U8",newline="\n"),ensure_ascii=False,indent=2)
		json.dump(d, fp=open(tpath,"w",encoding="U8",newline="\n"),ensure_ascii=False,indent=2)
	return d