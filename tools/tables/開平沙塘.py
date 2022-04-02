#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "yue_sy_kpsj"
	_file = "*開平沙塘*.tsv"
	tones = "33 1 1a 陰平 ꜀,55 3 2a 陰上 ꜂,,22 2 1b 陽平 ꜁,21 4 2b 陽上 ꜃,32 6 3b 陽去 ꜅,55 7a 4a 上陰入 ꜆,33 7b 4b 下陰入 ꜀,32 8 4c 陽入 ꜇,21 9 4d 變調入 ꜁"
	
	def parse(self, fs):
		if len(fs) < 8: return
		hz, _, _, sd, js, sm, ym = fs[:7]
		if ym and ym[-1] in "ptk":
			if sd == "1": sd = "9"
			elif sd == "2": sd = "7"
			elif sd == "5": sd = "10"
			elif sd == "6": sd = "8"
		yb = sm + ym + sd
		return hz, yb, js
