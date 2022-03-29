#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "nan_zq_qz"
	tones = "33 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,55 3 2a 陰上 ꜂,22 4 2b 陽上 ꜃,41 5 3a 陰去 ꜄,41 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,35 8 4b 陽入 ꜇"
	_file = "泉州调查字表*.tsv"

	def parse(self, fs):
		hz,py,yb,js = fs[:4]
		if not py: return
		tone = py[-1]
		if not tone.isdigit(): tone = ""
		yb = yb.rstrip("12345") + tone
		return hz, yb, js

