#!/usr/bin/env python3

from tables._表 import 表
from tables import hex2chr

bs = dict()
for line in open("/usr/share/unicode/CJKRadicals.txt"):
	line = line.strip()
	if not line or line.startswith("#"): continue
	fields = line.split("; ", 2)
	order, radical, han = fields
	han = hex2chr(han)
	bs[order] = han

class 字表(表):
	key = "bs"
	note = ""
	_lang = "部首餘筆"
	_file = "/usr/share/unicode/Unihan_IRGSources.txt"
	_sep = "\t"
	patches = {"□": "囗0", "〇": "乙0"}
	
	def parse(self, fs):
		if len(fs) < 3: return
		hz, typ, vals = fs
		if typ != "kRSUnicode": return
		hz = hex2chr(hz)
		l = list()
		for val in vals.split(" "):
			order, left = val.split(".")
			left = left.replace('-', 'f')
			js = bs[order]+left
			l.append(js)
		return hz, ",".join(l)
