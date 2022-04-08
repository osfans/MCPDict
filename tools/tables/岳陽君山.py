#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "cmn_xn_yyjs"
	_file = "岳阳君山.tsv"

	def parse(self, fs):
		sy, sd, _, hzs = fs[:4]
		if sd == "1a": sd = "1"
		elif sd == "1b": sd = "9"
		yb = sy + sd
		l = list()
		for hz, c, js in re.findall("(.)([-=?]?)(\[[^[]]*?\[[^[]]*?\][^[]]*?\]|\[.*?\])?", hzs):
			if js: js = js[1:-1]
			if hz == "~": hz = "□"
			l.append((hz, yb + c, js))
		return l
