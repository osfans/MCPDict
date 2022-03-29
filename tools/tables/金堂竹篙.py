#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "cmn_xn_jtzg"
	_file = "金堂竹篙.tsv"
	tones = "34 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,451 3 2 上 ꜂,,13 5 3 去 ꜄"
	simplified = 2

	def parse(self, fs):
		sy, sd, _, hzs = fs[:4]
		yb = sy + sd
		l = list()
		for hz, c, js in re.findall("(.)([-=?]?)(\[[^[]]*?\[[^[]]*?\][^[]]*?\]|\[.*?\])?", hzs):
			if js: js = js[1:-1]
			if hz == "~": hz = "□"
			l.append((hz, yb + c, js))
		return l
