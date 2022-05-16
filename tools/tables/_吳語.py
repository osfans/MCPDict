#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	tonesymbol = "¹²³⁴⁵"

	def parse(self, fs):
		if str(self) == "湖州":
			hz,_,yb,_,js = fs[:5]
		else:
			hz,_,yb,js = fs[:4]
		if len(hz) != 1: return
		if hz in "？☐": hz = "□"
		for i in self.tonesymbol:
			yb = yb.replace(i, str(self.tonesymbol.index(i) + 1))
		yb = self.dz2dl(yb)
		return hz, yb, js
