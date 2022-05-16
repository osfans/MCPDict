#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		name = str(self)
		if name in ("江陰", "江陰新橋", "江陰申港"):
			_,hz,js,yb = fs[:4]
			return hz, yb, js
		if name == "宣平":
			hz, _, yb, js = fs[:4]
			yb = self.dz2dl(yb)
			return hz, yb, js
		if name == "五峯":
			hz, yb = fs[:2]
			yb = self.dz2dl(yb)
			return hz, yb
		if name in ("南寧", "南寧亭子"):
			_,hz,_,yb,_,js,c = fs
			yb = self.dz2dl(yb)
			js = c + js
			return hz, yb, js
		return tuple(fs[:3])
