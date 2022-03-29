#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "hak_yt_mh_1900mh_ltc"
	_file = "1900s梅惠客家話字表.tsv"
	tones = " 1 1a 陰平 ꜀, 2 1b 陽平 ꜁, 3 2 上 ꜂,, 5 3 去 ꜄,, 7 4a 陰入 ꜆, 8 4b 陽入 ꜇"

	def parse(self, fs):
		hz,_,_,yb = fs[:4]
		return hz,yb
