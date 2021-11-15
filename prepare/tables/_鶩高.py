#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	def parse(self, fs):
		hz,jt,yb,py,js = fs[:5]
		sd = py[-1]
		if not sd.isdigit(): sd = ""
		yb = yb.rstrip("012345678") + sd
		js = js.rstrip('。')
		return hz, yb, js
