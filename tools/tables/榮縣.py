#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		hz,_,_,js,sm,ym,sd = fs[:7]
		if len(hz) != 1: return
		if sm in "ø": sm = ""
		l = list()
		for sd in sd.split("或"):
			yb = self.dz2dl(sm + ym, sd)
			l.append((hz, yb, js))
		return l

