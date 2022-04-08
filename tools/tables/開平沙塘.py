#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "yue_sy_kpsj"
	_file = "*開平沙塘*.tsv"
	
	def parse(self, fs):
		if len(fs) < 8: return
		hz, _, _, sd, js, sm, ym = fs[:7]
		if ym and ym[-1] in "ptk":
			if sd == "1": sd = "8"
			elif sd == "2": sd = "7"
			elif sd == "5": sd = "10"
			elif sd == "6": sd = "9"
		yb = sm + ym + sd
		return hz, yb, js
