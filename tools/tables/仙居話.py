#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	toneValues = {'334':1,'312':2,'423':3,'55':5,'22':6,'5':7,'2':8}

	def parse(self, fs):
		hz,_,yb,sd,js = fs[:5]
		if len(hz) != 1: return
		sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb, js
