#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_jq_wy"
	_file = "武義字表.tsv"

	def parse(self, fs):
		_,hz,py,yb,js = fs[:5]
		if not yb or len(hz)!=1: return
		sd = py[-1]
		if not sd.isdigit(): sd = ""
		yb = yb.rstrip("12345") + sd
		return hz, yb, js
