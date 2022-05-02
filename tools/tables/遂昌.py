#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	toneValues = {'55':1,'221':2,'52':3,'13':4,'334':5,'212':6,'5':7,'23':8}

	def parse(self, fs):
		hz,yb,sd,js = fs[:4]
		if not yb: return
		if sd == "0": sd = ""
		else: sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb, js
