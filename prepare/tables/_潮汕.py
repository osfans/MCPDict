#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	def parse(self, fs):
		hz, py, yb, js = fs[:4]
		yb = yb.replace(' ', '')
		if not yb: return
		sd = py[-1]
		if not sd.isdigit(): sd = ""
		yb = yb.rstrip("˩˨˧˦˥0") + sd
		return hz, yb, js
