#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_sl_sy"
	_file = "松阳方言字表*.tsv"
	toneValues = {'51':1,'31':2,'214':3,'22':4,'35':5,'13':6,'5':7,'2':8}

	def parse(self, fs):
		hz,_,yb,sd = fs[:4]
		if not yb: return
		if sd == "0": sd = ""
		else: sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb
