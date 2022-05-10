#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	def parse(self, fs):
		hz,yb,sd = fs[:3]
		if len(hz) != 1: return
		if not yb: return
		yb = self.dz2dl(yb, sd)
		return hz, yb
