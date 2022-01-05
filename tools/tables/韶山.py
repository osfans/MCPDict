#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "hsn_cayi_ss"
	note = "版本：2.0 (2021-12-22)<br>來源：王福堂.韶山方言同音字汇两种[J].方言, 2017(3):257-278.<br>轉錄者：跳跳老鼠"
	tones = "33 1 1a 陰平 ꜀,113 2 1b 陽平 ꜁,42 3 2 上 ꜂,,55 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅,24 7 4 入 ꜆"
	_file = "韶山方言字表*.tsv"
	disorder = True
	simplified = 2

	def parse(self, fs):
		_, yb, sd, hzs = fs[:4]
		yb = yb + sd
		l = list()
		for hz, c, js in re.findall("(.)([-=]?)(\[.*?\])?", hzs):
			if js: js = js[1:-1]
			l.append((hz, yb + c, js))
		return l

