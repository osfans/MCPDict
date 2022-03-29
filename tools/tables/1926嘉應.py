#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "hak_yt_mh_1926jy_ltc"
	_file = "1926嘉應客家話字表*.tsv"
	tones = "44 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,31 3 2 上 ꜂,,53 5 3 去 ꜄,,2 7 4a 陰入 ꜆,5 8 4b 陽入 ꜇"

	def parse(self, fs):
		hz,_,_,yb,js = fs[:5]
		return hz,yb,js
