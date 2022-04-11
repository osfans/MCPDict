#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	toneValues = {'陽入':8,'上':3,'陽平':2,'陰入':7,'陽去':6,'陰平':1,'陰去':5}

	def parse(self, fs):
		hz,_,yb,sd,js = fs
		if len(hz) != 1: return
		if not yb: return
		yb += str(self.toneValues[sd.split("|")[1]])
		return hz, yb, js
