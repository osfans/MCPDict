#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cmn_xn_cq_cs"
	_file = "重庆市长寿区方言字表*.tsv"
	tones = "45 1 1a 陰平 ꜀,212 2 1b 陽平 ꜁,331 3 2 上 ꜂,,15 5 3 去 ꜄"

	def parse(self, fs):
		_,hz,yb = fs[:3]
		if not yb: return
		return hz, yb
