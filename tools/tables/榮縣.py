#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cmn_xn_xs_jg_rx"
	_file = "青陽正韻*.tsv"
	toneValues = {"45":1,"21":2,"42":3, "214":5}
	_sep = '"\t"'
	
	def parse(self, fs):
		hz,_,_,js,sm,ym,sd = fs[:7]
		if not hz: return
		if sm in "ø": sm = ""
		l = list()
		for sd in sd.split("或"):
			yb = sm + ym + str(self.toneValues[sd])
			l.append((hz, yb, js))
		return l

