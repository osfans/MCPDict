#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	tonesymbol = "¹²³⁴⁵"

	def format(self,line):
		return line.replace("☐", "□")

	def parse(self, fs):
		hz,_,yb,js = fs
		for i in self.tonesymbol:
			yb = yb.replace(i, str(self.tonesymbol.index(i) + 1))
		_yb = yb.rstrip("12345")
		sd = yb[len(_yb):]
		sd = self.toneValues[sd]
		yb = _yb + str(sd)
		return hz, yb, js
