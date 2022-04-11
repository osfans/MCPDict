#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		hz,_,py,yb,js = fs[:5]
		sd = py[-1]
		sd = ord(sd) - ord("①") + 1
		yb = yb.rstrip("˩˨˧˦˥0") + str(sd)
		return hz, yb, js
