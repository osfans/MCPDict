#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	toneValues = {'44':1,'31':2,'45':3,'24':4,'42':5,'22':6,'323':7,'212':8}

	def parse(self, fs):
		hz,yb,sd = fs[:3]
		if len(hz) != 1: return
		if not yb: return
		sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb
