#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	def parse(self, fs):
		hz,_,yb,py,js = fs[:5]
		if not py: return
		sd = py[-1]
		if not sd.isdigit() or sd == "0": sd = ""
		yb = yb.rstrip("012345678¹²³⁴⁵") + sd
		js = js.rstrip('。')
		if hz == "？": hz = "□"
		return hz, yb, js
