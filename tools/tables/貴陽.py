#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cmn_xn_cq_qz_gy"
	tones = "45 1 1a 陰平 ꜀,21 2 1b 陽平 ꜁,43 3 2 上 ꜂,24 5 3 去 ꜄"
	_file = "黔中筑韵*.tsv"
	
	def parse(self, fs):
		hz, _, py, yb, js = fs[:5]
		sd = py[-1]
		if not sd.isdigit(): sd = ""
		yb = yb.rstrip("˩˨˧˦˥") + sd
		return hz, yb, js

