#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		hz,sy,sd,js = fs[:4]
		if not sy: return
		yb = self.dz2dl(sy, sd)
		return hz, yb, js
