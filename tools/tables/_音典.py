#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		name = str(self)
		if name in ("江陰", "江陰新橋", "江陰申港"):
			_,hz,js,yb = fs[:4]
			return hz, yb, js
		return tuple(fs[:3])
