#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_oj_wz"
	_file = "温州方言同音字表*.tsv"
	note = "版本：V2.0 (2021-09-10)<br>來源：<u>阿纓</u>整理自《溫州話音檔》"
	tones = "33 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,35 3 2a 陰上 ꜂,35 4 2b 陽上 ꜃,52 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,213 7 4a 陰入 ꜆,213 8 4b 陽入 ꜇"
	toneValues = {'阳入':8,'阴上':3,'阳平':2,'阴入':7,'阳去':6,'阴平':1,'阴去':5,'阳上':4}

	def parse(self, fs):
		_,hz,_,yb,_,_,sd,js = fs[:8]
		if not yb: return
		sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb, js
