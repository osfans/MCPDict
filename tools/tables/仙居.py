#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_tz_xj"
	_file = "仙居方言字表*.tsv"
	toneValues = {'334':1,'312':2,'423':3,'55':5,'22':6,'5':7,'2':8}

	def parse(self, fs):
		hz,_,yb,sd,js = fs[:5]
		sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb, js
