#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_oj_wz_ltc"
	_file = "清末温州*.tsv"
	tones = "44 1 1a 陰平 ꜀,331 2 1b 陽平 ꜁,45 3 2a 陰上 ꜂,34 4 2b 陽上 ꜃,42 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,223 7 4a 陰入 ꜆,112 8 4b 陽入 ꜇"

	def parse(self, fs):
		if len(fs) < 7: return
		_,hz,yb,_,_,_,js = fs[:7]
		return hz, yb, js
