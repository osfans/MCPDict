#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "nan_cs_hllj"
	note = "版本：V1.07 (2021-12-25)<br>來源：<u>空白</u>"
	tones = "35 1 1a 陰平 ꜀,44 2 1b 陽平 ꜁,53 3 2a 陰上 ꜂,11 4 2b 陽上 ꜃,41 5 3 去 ꜄,,2 7 4a 陰入 ꜆,5 8 4b 陽入 ꜇"
	_file = "惠来隆江字表*.tsv"
	toneNames = {'阳入':8,'阴上':3,'阳平':2,'阴入':7,'阴平':1,'去声':5,'阳上':4}

	def parse(self, fs):
		_,hz,_,yb,js = fs[:5]
		if not yb: return
		for i in self.toneNames:
			if i in yb:
				yb = yb.replace(i, str(self.toneNames[i]))
				break
		return hz, yb, js

