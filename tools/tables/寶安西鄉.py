#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "yue_gf_baxx"
	_file = "深圳西乡粤语字表.tsv"
	
	def parse(self, fs):
		if len(fs) < 9: return
		hz, sd, yb, js = fs[0], fs[4], fs[7], fs[8]
		yb = yb.rstrip("¹²³⁴⁵") + sd
		return hz, yb, js
