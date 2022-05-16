#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		if len(fs) < 7: return
		_,hz,yb,_,_,sd,js = fs[:7]
		yb = yb + sd
		return hz, yb, js
