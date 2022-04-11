#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	
	def parse(self, fs):
		hz, _, py, yb, js = fs[:5]
		sd = py[-1]
		if not sd.isdigit(): sd = ""
		yb = yb.rstrip("˩˨˧˦˥") + sd
		return hz, yb, js

