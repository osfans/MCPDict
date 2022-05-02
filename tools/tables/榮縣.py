#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	toneValues = {"45":1,"21":2,"42":3, "214":5}
	#_sep = "\t"
	
	def parse(self, fs):
		hz,_,_,js,sm,ym,sd = fs[:7]
		if len(hz) != 1: return
		if sm in "ø": sm = ""
		l = list()
		for sd in sd.split("或"):
			yb = sm + ym + str(self.toneValues[sd])
			l.append((hz, yb, js))
		return l

