#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "hsn_cayi_hs"
	tones = "33 1 1a 陰平 ꜀,21 2 1b 陽平 ꜁,13 3 2 上 ꜂,,45 5 3a 陰去 ꜄,34 6 3b 陽去 ꜅,24 7 4 入 ꜆"
	_file = "衡山話字表*.tsv"
	disorder = True

	def parse(self, fs):
		_, yb, sd, hzs = fs[:4]
		yb = yb + sd
		l = list()
		for hz, c, js in re.findall("(.)([-=]?)(\[.*?\])?", hzs):
			if js: js = js[1:-1]
			l.append((hz, yb + c, js))
		return l

