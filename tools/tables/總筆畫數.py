#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables import hex2chr

class 表(_表):
	_file = "Unihan_IRGSources.txt"
	_sep = "\t"
	patches = {"□": "3", "〇": "1"}
	
	def parse(self, fs):
		if len(fs) < 3: return
		hz, typ, yb = fs
		if typ != "kTotalStrokes": return
		hz = hex2chr(hz)
		return hz, yb
