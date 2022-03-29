#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "hsn_ls_sy"
	tones = "55 1 1a 陰平 ꜀,12 2 1b 陽平 ꜁,42 3 2 上 ꜂,,35 5 3a 陰去 ꜄,24 6 3b 陽去 ꜅,33 7 4 入 ꜆"
	_file = "邵陽.tsv"
	disorder = True
	simplified = 2

	def parse(self, fs):
		_, yb, sd, dz, hzs = fs[:5]
		yb = yb + sd
		l = list()
		for hz, c, js in re.findall("(.)([-=]?)(\[[^[]]*?\[[^[]]*?\][^[]]*?\]|\[.*?\])?", hzs):
			if js: js = js[1:-1]
			if hz == "~": hz = "□"
			l.append((hz, yb + c, js))
		return l

