#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	tonesymbol = "¹²³⁴⁵"

	def format(self,line):
		return line.replace("☐", "□")

	def parse(self, fs):
		hz,_,yb,js = fs
		if len(hz) != 1: return
		for i in self.tonesymbol:
			yb = yb.replace(i, str(self.tonesymbol.index(i) + 1))
		_yb = yb.rstrip("12345")
		sd = yb[len(_yb):]
		sd = self.toneValues[sd]
		yb = _yb + str(sd)
		return hz, yb, js
