#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	#https://github.com/nk2028/sino-korean-readings/blob/main/woosun-sin.csv
	isYb = False

	def parse(self, fs):
		hz = fs[0]
		py = "".join(fs[1:4])
		return hz, py
