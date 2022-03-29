#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_sl_yh"
	_file = "云和方言同音字表*.tsv"
	tones = "324 1 1a 陰平 ꜀,423 2 1b 陽平 ꜁,53 3 2a 陰上 ꜂,21 4 2b 陽上 ꜃,55 5 3a 陰去 ꜄,223 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,24 8 4b 陽入 ꜇"
	toneValues = {'324':1,'423':2,'53':3,'21':4,'55':5,'223':6,'5':7,'24':8}

	def parse(self, fs):
		hz,_,yb,sd,js = fs[:5]
		if not yb: return
		if sd == "0": sd = ""
		else: sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb, js
