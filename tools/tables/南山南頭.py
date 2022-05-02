#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	
	def parse(self, fs):
		if len(fs) < 6: return
		hz, py, yb, js = fs[0], fs[3], fs[4], fs[5]
		sd = py[-1]
		if sd == "h": sd = "9"
		yb = yb.rstrip("¹²³⁴⁵") + sd
		return hz, yb, js
