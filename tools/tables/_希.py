#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	_sep = '"\t"'
	def parse(self, fs):
		hz, _, yb, js = fs[:4]
		sy = yb.rstrip("˩˨˧˦˥")
		sd = yb[len(sy):]
		if sd not in self.toneValues:
			print("\t\t\t", fs)
			return
		sd = self.toneValues[sd]
		yb = sy + str(sd)
		return hz, yb, js

