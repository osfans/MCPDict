#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "nan_cs_rp"
	_file = "方言调查字表（闽-饶平）*.tsv"

	def parse(self, fs):
		hz,py,yb,js = fs[:4]
		if not yb: return
		sd = py[-1]
		if not sd.isdigit(): sd = ""
		yb = yb.rstrip("˩˨˧˦˥0") + sd
		return hz, yb, js
