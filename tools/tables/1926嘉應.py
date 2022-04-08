#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "hak_yt_mh_1926jy_ltc"
	_file = "1926嘉應客家話字表*.tsv"

	def parse(self, fs):
		hz,_,_,yb,js = fs[:5]
		return hz,yb,js
