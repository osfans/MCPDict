#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	def parse(self, fs):
		hz,_,sy,sd,js = fs[:5]
		if len(hz) != 1: return
		yb = self.dz2dl(sy, sd)
		return hz, yb, js
