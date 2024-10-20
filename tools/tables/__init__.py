#!/usr/bin/env python3

import re, os, json, glob
from importlib import import_module
import tables._詳情
from opencc import OpenCC

xing_keys = ["漢字","兩分","字形描述","五筆畫","說文","康熙","匯纂","漢大"]
xing_keys_len = len(xing_keys)
def hex2chr(uni):
	"把unicode轉換成漢字"
	if uni.startswith("U+"): uni = uni[2:]
	return chr(int(uni, 16))

t2s_dict = {
	"鄕":"鄉",
	"玆":"兹",
	"淸":"清",
	"尙":"尚",
	"髙":"高",
	"靑":"青",
	"楡":"榆",
	"舖":"鋪",
	"谿":"溪",
}
opencc = OpenCC("t2s.json")
def t2s(s, prepare = True):
	for i in t2s_dict:
		s = s.replace(i,t2s_dict[i])
	if prepare:
		return s
	return opencc.convert(s)

def cjkorder(s):
	n = ord(s)
	return n + 0x10000 if n < 0x4E00 else n

def addAllFq(d, fq, order,ignorePian = False):
	if order is None or fq is None: return
	fqs = fq.split(",")[0].split("-")
	for i in range(len(fqs)):
		name = "-".join(fqs[0:i+1])
		if not name or name in d: continue
		if ignorePian and name.endswith("片"): continue
		d[name] = "-".join(order.split("-")[0:i+1])

def addCfFq(d, fq, order):
	if fq is None: return
	fqs = fq.split(",")[2:]
	for i,fq in enumerate(fqs):
		if not fq: continue
		if fq not in d:
			d[fq] = i, order
		#if i not in d: d[i] = dict()
		# if fq not in d[i]:
		# 	d[i][fq] = order

def getLangsByArgv(infos, argv):
	l = []
	for a in argv:
		if a in infos:
			l.append(a)
		elif os.path.isfile(a):
			path = os.path.dirname(a)
			for i in infos:
				if a in glob.glob(os.path.join(path, infos[i]["文件名"])):
					l.append(i)
					break
	return l

def getLangs(dicts, argv=None):
	infos = tables._詳情.load()
	langs = []
	count = 0
	if argv:
		mods = ["漢字"]
		mods.extend(getLangsByArgv(infos, argv))
	else:
		mods = xing_keys.copy()
		mods.extend(argv if argv else infos.keys())
		lb = ["總筆畫數","部首餘筆","倉頡三代","倉頡五代","倉頡六代","五筆86版","五筆98版","五筆06版","異體字","字形變體","分類"]
		mods.extend(lb)
	types = [dict(),dict(),dict(),dict(),dict()]
	keys = None
	for mod in mods:
		if mod in infos:
			d = infos[mod]
			try:
				if d["文件格式"]:
					lang = import_module(f'tables._{d["文件格式"]}').表()
					lang.setmod(mod)
				else:
					lang = import_module(f"tables.{mod}").表()
				if not lang._file: lang._file = d["文件名"]
			except Exception as e:
				print(f"\t\t\t{e} {mod}")
				continue
			if d["簡繁"] == "简": lang.simplified = 2
			if d["地圖集二分區"] == None: d["地圖集二分區"] = ""
			addAllFq(types[0], d["地圖集二分區"], d["地圖集二排序"])
			addAllFq(types[1], d["音典分區"], d["音典排序"])
			if d["省"]:
				addAllFq(types[1], d["省"], "ZZZZ")
				if not d["音典分區"]: d["音典分區"] = ""
				d["音典分區"] +=  "," + d["省"]
			addCfFq(types[2], d["陳邡分區"], d["陳邡排序"])
			if d["聲調"]:
				toneMaps = dict()
				sds = json.loads(d["聲調"])
				for i in sds:
					tv = sds[i][0]
					if tv in toneMaps and "入" in sds[i][3]:
						tv += "0"
					toneMaps[tv] = i
				lang.toneMaps = toneMaps
			lang.info = d
			lang.load(dicts)
			if d["文件名"] != "mcpdict.db":
				if lang.count == 0: continue
				if lang.count < 900:
					print(f"\t\t\t字數太少 {mod}")
				elif lang.syCount < 100:
					print(f"\t\t\t音節太少 {mod}")
			lang.info["文件名"] = lang._file
			count += 1
		else:
			lang = import_module(f"tables.{mod}").表()
			d = dict()
			d["語言"] = lang.full if lang.full else mod
			d["簡稱"] = lang.short if lang.short else mod
			d["地圖集二顏色"] = lang.color if count == 0 else None
			d["地圖集二分區"] = None
			lang.info = d
			lang.load(dicts)
		lang.info["字數"] = lang.count
		lang.info["□數"] = lang.unknownCount if lang.unknownCount else None
		sydCount = lang.sydCount
		syCount = lang.syCount
		lang.info["音節數"] = sydCount if sydCount else None
		lang.info["不帶調音節數"] = syCount if syCount and syCount != sydCount else None
		lang.info["網站"] = lang.site
		lang.info["網址"] = lang.url
		lang_t = lang.info["語言"]
		lang_s = t2s(lang.info["語言"], True)
		if lang_s not in lang_t:
			lang_t += f",{lang_s}"
		lang_s = t2s(lang.info["語言"], False)
		if lang_s not in lang_t:
			lang_t += f",{lang_s}"
		lang.info["語言索引"] = lang_t
		if lang.note: lang.info["說明"] = lang.note
		if not keys: keys = lang.info.keys()
		langs.append(lang)
	hz = langs[0]
	for i in keys:
		if i not in hz.info: hz.info[i] = None
	hz.info["字數"] = len(dicts)
	hz.info["說明"] = "字數：%d<br>語言數：%d<br><br>%s"%(len(dicts), count, hz.note)
	hz.info["地圖集二分區"] = ",".join(sorted(types[0].keys(),key=lambda x:(x.count("-"),types[0][x])))
	hz.info["音典分區"] = ",".join(sorted(types[1].keys(),key=lambda x:types[1][x]))
	hz.info["陳邡分區"] = ",".join(sorted(types[2].keys(),key=lambda x:types[2][x]))
	print("語言數", count)
	return langs
