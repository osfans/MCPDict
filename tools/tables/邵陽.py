#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "hsn_ls_sy"
	_file = "邵陽.tsv"
	disorder = True

	def parse(self, fs):
		_, yb, sd, dz, hzs = fs[:5]
		yb = yb + sd
		l = list()
		for hz, c, js in re.findall("(.)([-=]?)(\[[^[]]*?\[[^[]]*?\][^[]]*?\]|\[.*?\])?", hzs):
			if js: js = js[1:-1]
			if hz == "~": hz = "□"
			l.append((hz, yb + c, js))
		return l

