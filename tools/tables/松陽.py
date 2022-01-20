#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_sl_sy"
	_file = "松阳方言字表*.tsv"
	note = "來源：<u>落橙</u>"
	tones = "51 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,214 3 2a 陰上 ꜂,22 4 2b 陽上 ꜃,35 5 3a 陰去 ꜄,13 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"
	toneValues = {'51':1,'31':2,'214':3,'22':4,'35':5,'13':6,'5':7,'2':8}

	def parse(self, fs):
		hz,_,yb,sd = fs[:4]
		if not yb: return
		if sd == "0": sd = ""
		else: sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb
