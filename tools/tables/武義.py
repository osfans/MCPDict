#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		_,hz,py,yb,js = fs[:5]
		if not yb or len(hz)!=1: return
		sd = py[-1]
		if not sd.isdigit(): sd = ""
		yb = yb.rstrip("12345") + sd
		return hz, yb, js
