#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "nan_zq_xm"
	_file = "方言调查字表（厦门）*.tsv"
	tones = "44 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,53 3 2 上 ꜂,,21 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,32 7 4a 陰入 ꜆,4 8 4b 陽入 ꜇"

	def parse(self, fs):
		hz,py,yb,js = fs[:4]
		if not py: return
		tone = py[-1]
		if not tone.isdigit(): tone = ""
		yb = yb.rstrip("12345") + tone
		return hz, yb, js

