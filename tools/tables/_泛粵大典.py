#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	
	def parse(self, fs):
		if len(fs) < 4: return
		js = ""
		hz = fs[0]
		yb = fs[14] + fs[15] + fs[4]
		js = fs[17]
		return hz, yb, js
