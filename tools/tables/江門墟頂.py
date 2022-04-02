#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "yue_sy_jmxd"
	_file = "*江門蓬江墟頂*.tsv"
	tones = "23 1 1a 陰平陰去 ꜀,45 3 2a 陰上 ꜂,,21 2 1b 陽平陽上 ꜁,,32 6 3b 陽去 ꜅,45 7a 4a 上陰入 ꜆,23 7b 4b 下陰入 ꜀,32 8 4c 陽入 ꜇,21 9 4d 變入 ꜁"
	
	def parse(self, fs):
		if len(fs) < 8: return
		hz, _, _, _, sd, js, sm, jy, ym = fs[:9]
		if ym and ym[-1] in "ptk":
			if sd == "1": sd = "9"
			elif sd == "2": sd = "7"
			elif sd == "4": sd = "10"
			elif sd == "6": sd = "8"
		if jy == "i" and ym.startswith("i"): jy = ""
		yb = sm + jy + ym + sd
		return hz, yb, js
