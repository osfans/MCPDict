#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):

	def parse(self, fs):
		yb, sd, hzs = fs[:3]
		yb = yb + sd
		l = list()
		for hz, c, js in re.findall("(.)([-=?]?)(\[[^[]]*?\[[^[]]*?\][^[]]*?\]|\[.*?\])?", hzs):
			if js: js = js[1:-1]
			if hz == "~": hz = "□"
			l.append((hz, yb + c, js))
		return l
