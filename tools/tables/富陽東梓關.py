#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_th_ls_fydzg"
	_file = "东梓关字表*.tsv"
	tones = "53 1 1a 陰平 ꜀,223 2 1b 陽平 ꜁,55 3 2 上 ꜂,,335 5 3a 陰去 ꜄,313 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"

	def format(self, line):
		return line.replace('�','□')

	def parse(self, fs):
		_, hz, js, yb, py = fs[:5]
		sd = py[-1]
		if not sd.isdigit() or sd == "0": sd = ""
		yb = yb.rstrip("¹²³⁴⁵")
		yb += sd
		return hz, yb, js
