#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cdo_nd"
	_file = "闽东宁德方言字表*.tsv"
	note = "來源：<u>落橙</u>、<u>阿纓</u>"
	tones = "44 1 1a 陰平 ꜀,22 2 1b 陽平 ꜁,42 3 2 上 ꜂,,35 5 3a 陰去 ꜄,332 6 3b 陽去 ꜅,2 7 4a 陰入 ꜆,5 8 4b 陽入 ꜇"
	toneValues = {'陽入':8,'上':3,'陽平':2,'陰入':7,'陽去':6,'陰平':1,'陰去':5}

	def parse(self, fs):
		hz,_,yb,sd,js = fs
		if not yb: return
		yb += str(self.toneValues[sd.split("|")[1]])
		return hz, yb, js
