#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	
	def parse(self, fs):
		if len(fs) < 8: return
		hz, _, _, _, sd, js, sm, jy, ym = fs[:9]
		if ym and ym[-1] in "ptk":
			if sd == "1": sd = "8"
			elif sd == "2": sd = "7"
			elif sd == "4": sd = "10"
			elif sd == "6": sd = "9"
		if jy == "i" and ym.startswith("i"): jy = ""
		yb = sm + jy + ym + sd
		return hz, yb, js
