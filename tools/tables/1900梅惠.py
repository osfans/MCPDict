#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "hak_yt_mh_1900mh_ltc"
	_file = "1900s梅惠客家話字表.tsv"

	def parse(self, fs):
		hz,_,_,yb = fs[:4]
		return hz,yb
