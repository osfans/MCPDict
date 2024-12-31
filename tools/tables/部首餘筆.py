#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables import hex2chr

class 表(_表):
	說明 = ""
	_file = "Unihan_IRGSources.txt"
	_sep = "\t"
	patches = {"□": "囗0", "〇": "乙0"}
	bs = dict()
	
	def __init__(自):
		_表.__init__(自)
		for 行 in open(自.全路徑("CJKRadicals.txt"),encoding="U8"):
			行 = 行.strip()
			if not 行 or 行.startswith("#"): continue
			fields = 行.split("; ", 2)
			order, radical, han = fields
			han = hex2chr(han)
			自.bs[order] = han

	def 析(自, 列):
		if len(列) < 3: return
		字, typ, vals = 列
		if typ != "kRSUnicode": return
		字 = hex2chr(字)
		l = list()
		for val in vals.split(" "):
			order, left = val.split(".")
			left = left.replace('-', 'f')
			js = 自.bs[order]+left
			l.append(js)
		return 字, ",".join(l)
