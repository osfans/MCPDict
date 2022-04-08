#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_oj_wz_ltc"
	_file = "清末温州*.tsv"

	def parse(self, fs):
		if len(fs) < 7: return
		_,hz,yb,_,_,_,js = fs[:7]
		return hz, yb, js
