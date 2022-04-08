#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_jy"
	_file = "缙云字表*.tsv"
	toneValues = {'阳入':8,'阴上':3,'阳平':2,'阴入':7,'阳去':6,'阴平':1,'阴去':5,'阳上':4}

	def parse(self, fs):
		hz,sd,js,yb = fs[0],fs[4],fs[5],fs[7]
		yb = yb.rstrip("˩˨˧˦˥0") + str(self.toneValues[sd])
		return hz, yb, js
