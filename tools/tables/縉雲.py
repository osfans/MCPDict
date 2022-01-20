#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_jy"
	_file = "缙云字表*.tsv"
	note = "來源：由東甌組<u>老虎</u>、<u>林奈安</u>提供"
	tones = "334 1 1a 陰平 ꜀,231 2 1b 陽平 ꜁,53 3 2a 陰上 ꜂,31 4 2b 陽上 ꜃,554 5 3a 陰去 ꜄,213 6 3b 陽去 ꜅,423 7 4a 陰入 ꜆,35 8 4b 陽入 ꜇"
	toneValues = {'阳入':8,'阴上':3,'阳平':2,'阴入':7,'阳去':6,'阴平':1,'阴去':5,'阳上':4}
	simplified = 2

	def parse(self, fs):
		hz,sd,js,yb = fs[0],fs[4],fs[5],fs[7]
		yb = yb.rstrip("˩˨˧˦˥0") + str(self.toneValues[sd])
		return hz, yb, js
