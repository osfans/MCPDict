#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables import hex2chr

class 表(_表):
	note = ""
	_file = "Unihan_IRGSources.txt"
	_sep = "\t"
	patches = {"□": "囗0", "〇": "乙0"}
	bs = dict()
	
	def __init__(self):
		_表.__init__(self)
		for line in open(self.get_fullname("CJKRadicals.txt"),encoding="U8"):
			line = line.strip()
			if not line or line.startswith("#"): continue
			fields = line.split("; ", 2)
			order, radical, han = fields
			han = hex2chr(han)
			self.bs[order] = han

	def parse(self, fs):
		if len(fs) < 3: return
		hz, typ, vals = fs
		if typ != "kRSUnicode": return
		hz = hex2chr(hz)
		l = list()
		for val in vals.split(" "):
			order, left = val.split(".")
			left = left.replace('-', 'f')
			js = self.bs[order]+left
			l.append(js)
		return hz, ",".join(l)
