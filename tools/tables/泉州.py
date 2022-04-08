#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "nan_zq_qz"
	_file = "泉州调查字表*.tsv"

	def parse(self, fs):
		hz,py,yb,js = fs[:4]
		if not py: return
		tone = py[-1]
		if not tone.isdigit(): tone = ""
		yb = yb.rstrip("12345") + tone
		return hz, yb, js

