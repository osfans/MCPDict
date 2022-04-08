#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "gan_fg_yh"
	_file = "宜黄.tsv"

	def parse(self, fs):
		yb, sd, hzs = fs[:3]
		yb = yb + sd
		l = list()
		for hz, c, js in re.findall("(.)([-=?]?)(\[[^[]]*?\[[^[]]*?\][^[]]*?\]|\[.*?\])?", hzs):
			if js: js = js[1:-1]
			if hz == "~": hz = "□"
			l.append((hz, yb + c, js))
		return l
