#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	def parse(self, fs):
		hz,_,yb,js,sm,ym,sd = fs[:7]
		if not yb: return
		yb = self.dz2dl(sm+ym, sd)
		return hz, yb, js
