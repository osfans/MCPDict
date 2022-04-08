#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cdo_nd"
	_file = "闽东宁德方言字表*.tsv"
	toneValues = {'陽入':8,'上':3,'陽平':2,'陰入':7,'陽去':6,'陰平':1,'陰去':5}

	def parse(self, fs):
		hz,_,yb,sd,js = fs
		if not yb: return
		yb += str(self.toneValues[sd.split("|")[1]])
		return hz, yb, js
