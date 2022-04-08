#!/usr/bin/env python3

def hex2chr(uni):
	"把unicode轉換成漢字"
	if uni.startswith("U+"): uni = uni[2:]
	return chr(int(uni, 16))

def cjkorder(s):
	n = ord(s)
	return n + 0x10000 if n < 0x4E00 else n

import tables.info
import importlib
def getLangs(dicts, keys=None):
	d = tables.info.getInfos()
	langs = []
	if keys:
		l = keys
	else:
		l = ["漢字","兩分","五筆畫","說文","康熙","漢大"]
		l.extend(d.keys())
		lb = ["總筆畫數","部首餘筆","倉三","倉五","倉六","五筆86","五筆98","五筆06","異體字","分類"]
		l.extend(lb)
	for mod in l:
		try:
			lang = importlib.import_module(f"tables.{mod}").字表()
		except Exception as e:
			print(f"\t\t\t{e}")
			continue
		if mod in d:
			lang.lang, lang.color, lang.ver, lang.location, lang.size, lang.editor, lang.book, note, jf, lang.tones = d[mod]
			if note: lang.note = note
			if jf == "簡": lang.simplified = 2
		lang.load(dicts)
		langs.append(lang)

	return langs
