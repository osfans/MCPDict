#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables import hex2chr

class 表(_表):
	_file = "Unihan_IRGSources.txt"
	_sep = "\t"
	patches = {"□": "3", "〇": "1"}
	
	def 析(自, 列):
		if len(列) < 3: return
		字, typ, yb = 列
		if typ != "kTotalStrokes": return
		字 = hex2chr(字)
		return 字, yb
