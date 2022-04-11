#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	toneValues = {"13":1,"11":2,"21":3,"45":5}

	def parse(self, fs):
		hz,_,_,_,_,sd,_,yb,js = fs[:9]
		if len(hz) != 1: return
		sd = str(self.toneValues[sd])
		yb = yb.rstrip("012345") + sd
		return hz, yb, js
