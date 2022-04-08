#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cmn_xn_cq_cs"
	_file = "重庆市长寿区方言字表*.tsv"

	def parse(self, fs):
		_,hz,yb = fs[:3]
		if not yb: return
		return hz, yb
