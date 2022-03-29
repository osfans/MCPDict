#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_sl_tsly"
	_file = "泰顺罗阳同音字表*.tsv"
	tones = "224 1 1a 陰平 ꜀,42 2 1b 陽平 ꜁,51 3 2a 陰上 ꜂,21 4 2b 陽上 ꜃,35 5 3a 陰去 ꜄,11 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,1 8 4b 陽入 ꜇,33 0 0 小稱 0"
	toneValues = {'224':1,'42':2,'51':3,'21':4,'35':5,'11':6,'5':7,'1':8, '33':9}

	def parse(self, fs):
		hz,_,yb,sd,js = fs[:5]
		if not yb: return
		if sd == "0": sd = ""
		else: sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb, js
