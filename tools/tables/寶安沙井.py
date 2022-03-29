#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "yue_gf_basj"
	_file = "沙井粤语字表*.tsv"
	tones = "35 1 1a 上陰平 ꜀,21 2 1c 陽平 ꜁,11 3 2 上 ꜂,,24 5 3a 陰去 ꜄,32 6 3b 陽去 ꜅,55 7 4a 陰入 ꜆,211 8 4b 陽入 ꜇,55 1 1b 下陰平 ꜆"
	
	def parse(self, fs):
		if len(fs) < 9: return
		hz, sd, yb, js = fs[0], fs[4], fs[7], fs[8]
		if sd == "1h": sd = "9"
		yb = yb.rstrip("¹²³⁴⁵") + sd
		return hz, yb, js
