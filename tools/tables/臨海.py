#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_tz_lh"
	_file = "临海方言字表*.tsv"
	toneValues = {'33':1,'31':2,'42':3,'55':5,'13':6,'5':7,'2':8}

	def parse(self, fs):
		hz,_,yb,sd,js = fs[:5]
		sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb, js
