#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_ll_xybbg"
	_file = "孝义白壁关*.tsv"
	tones = "24 1 1a 陰平 ꜀,11 2 1b 陽平 ꜁,312 3 2 上 ꜂,,55 5 3 去 ꜄,,2 7 4a 陰入 ꜆,312 8 4b 陽入 ꜇"
	simplified = 2
	
	def format(self, line):
		return line.replace(" #", "#").replace("ø", "")
