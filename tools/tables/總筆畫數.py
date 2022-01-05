#!/usr/bin/env python3

from tables._表 import 表
from tables import hex2chr

class 字表(表):
	key = "bh"
	note = ""
	_lang = "總筆畫數"
	_file = "/usr/share/unicode/Unihan_IRGSources.txt"
	_sep = "\t"
	patches = {"□": "3", "〇": "1"}
	
	def parse(self, fs):
		if len(fs) < 3: return
		hz, typ, yb = fs
		if typ != "kTotalStrokes": return
		hz = hex2chr(hz)
		return hz, yb
