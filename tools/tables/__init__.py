#!/usr/bin/env python3

import re, os, json
from importlib import import_module
import tables._詳情

def hex2chr(uni):
	"把unicode轉換成漢字"
	if uni.startswith("U+"): uni = uni[2:]
	return chr(int(uni, 16))

def cjkorder(s):
	n = ord(s)
	return n + 0x10000 if n < 0x4E00 else n

def addAllFq(d, fq, order,ignorePian = False):
	if order is None or fq is None: return
	fqs = fq.split("-")
	for i in range(len(fqs)):
		name = "-".join(fqs[0:i+1])
		if not name or name in d: continue
		if ignorePian and name.endswith("片"): continue
		d[name] = "-".join(order.split("-")[0:i+1])

def addCf2Fq(d, fq, order):
	if fq is None: return
	fqs = fq.split(",")
	for i,fq in enumerate(fqs):
		if not fq: continue
		if i not in d: d[i] = dict()
		if fq not in d[i]:
			d[i][fq] = order

def getLangs(dicts, argv=None):
	infos = tables._詳情.load()
	langs = []
	count = 0
	if argv:
		mods = ["漢字"]
		mods.extend(argv)
	else:
		mods = ["漢字","兩分","五筆畫","說文解字","康熙字典","漢語大字典"]
		mods.extend(argv if argv else infos.keys())
		lb = ["總筆畫數","部首餘筆","倉頡三代","倉頡五代","倉頡六代","五筆86版","五筆98版","五筆06版","異體字","分類"]
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
			if d["簡繁"] == "簡": lang.simplified = 2
			addAllFq(types[0], d["地圖集二分區"], d["地圖集二排序"])
			addAllFq(types[1], d["音典分區"], d["音典排序"])
			if d["省"]:
				addAllFq(types[1], d["省"], "ZZZZ")
				d["音典分區"] += "," + d["省"]
			addAllFq(types[2], d["陳邡分區"], d["陳邡排序"], True)
			addAllFq(types[4], d["俞銓（正心）分區"], d["俞銓（正心）排序"], True)
			addCf2Fq(types[3], d["陳邡二分區"], d["陳邡二排序"])
			lang.info = d
			lang.load(dicts)
			if lang.count == 0: continue
			count += 1
		else:
			lang = import_module(f"tables.{mod}").表()
			d = dict()
			d["語言"] = mod
			d["簡稱"] = lang.short if lang.short else mod
			d["地圖集二顏色"] = lang.color if count == 0 else None
			d["地圖集二分區"] = None
			lang.info = d
			lang.load(dicts)
		lang.info["字數"] = lang.count
		sydCount = lang.sydCount
		syCount = lang.syCount
		lang.info["音節數"] = sydCount if sydCount else None
		lang.info["不帶調音節數"] = syCount if syCount and syCount != sydCount else None
		lang.info["網站"] = lang.site
		lang.info["網址"] = lang.url
		lang.info["說明"] = lang.note if lang.note else None
		if not keys: keys = lang.info.keys()
		langs.append(lang)
	hz = langs[0]
	for i in keys:
		if i not in hz.info: hz.info[i] = None
	hz.info["字數"] = len(dicts)
	hz.info["說明"] = "字數：%d<br>語言數：%d<br><br>%s"%(len(dicts), count, hz.note)
	hz.info["地圖集二分區"] = ",".join(sorted(types[0].keys(),key=lambda x:(x.count("-"),types[0][x])))
	hz.info["音典分區"] = ",".join(sorted(types[1].keys(),key=lambda x:types[1][x]))
	hz.info["陳邡分區"] = ",".join(sorted(types[2].keys(),key=lambda x:(x.count("-"),types[2][x])))
	hz.info["俞銓（正心）分區"] = ",".join(sorted(types[4].keys(),key=lambda x:(x.count("-"),types[4][x])))
	hz.info["陳邡二分區"] = ",".join(",".join(sorted(types[3][i].keys(),key=lambda x:types[3][i][x])) for i in types[3])
	print("語言數", count)
	return langs
