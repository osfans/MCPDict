#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "hak_whhb"
	_file = "五华横陂客家方言字表*.tsv"
	tones = "44 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,31 3 2 上 ꜂,,53 5 3 去 ꜄,,1 7 4a 陰入 ꜆,5 8 4b 陽入 ꜇"
	toneValues = {'44':1,'13':2,'31':3,'53':5,'1':7,'5':8}

	def parse(self, fs):
		hz,yb,sd,js = fs[:4]
		if not yb: return
		if sd == "0": sd = ""
		else: sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb, js
