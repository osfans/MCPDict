#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "gan_fg_yh"
	_file = "宜黄.tsv"
	tones = "22 1 1a 陰平 ꜀,53 2 1b 陽平 ꜁,242 3 2 上 ꜂,,42 5 3a 陰去 ꜄,13 6 3b 陽去 ꜅,2 7 4a 陰入 ꜆,5 8 4b 陽入 ꜇"
	simplified = 2

	def parse(self, fs):
		yb, sd, hzs = fs[:3]
		yb = yb + sd
		l = list()
		for hz, c, js in re.findall("(.)([-=?]?)(\[[^[]]*?\[[^[]]*?\][^[]]*?\]|\[.*?\])?", hzs):
			if js: js = js[1:-1]
			if hz == "~": hz = "□"
			l.append((hz, yb + c, js))
		return l
