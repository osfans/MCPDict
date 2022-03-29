#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "yue_sy_kpsj"
	_file = "*開平沙塘*.tsv"
	tones = "35 1 1a 上陰平 ꜀,21 2 1c 陽平 ꜁,11 3 2 上 ꜂,,24 5 3a 陰去 ꜄,32 6 3b 陽去 ꜅,55 7 4a 陰入 ꜆,211 8 4b 陽入 ꜇,55 1 1b 下陰平 ꜆"
	
	def parse(self, fs):
		if len(fs) < 8: return
		hz, _, _, sd, js, sm, ym = fs[:7]
		yb = sm + ym + sd
		return hz, yb, js
