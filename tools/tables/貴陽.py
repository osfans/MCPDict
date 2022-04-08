#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cmn_xn_cq_qz_gy"
	_file = "黔中筑韵*.tsv"
	
	def parse(self, fs):
		hz, _, py, yb, js = fs[:5]
		sd = py[-1]
		if not sd.isdigit(): sd = ""
		yb = yb.rstrip("˩˨˧˦˥") + sd
		return hz, yb, js

