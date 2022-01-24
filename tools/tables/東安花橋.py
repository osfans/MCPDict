#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "wxa_xn_dahq"
	note = "來源：鮑厚星.東安土話研究[M].湖南教育出版社.1998<br>轉錄者：跳跳老鼠"
	tones = "33 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,54 3 2 上 ꜂,,35 5 3a 陰去 ꜄,24 6 3b 陽去 ꜅,42 7 4 入 ꜆"
	_file = "東安花橋.tsv"
	disorder = True
	simplified = 0

	def parse(self, fs):
		_, yb, sd, hzs = fs[:4]
		yb = yb + sd
		l = list()
		for hz, c, js in re.findall("(.)([-=]?)(\[.*?\[.*?\].*?\]|\[.*?\])?", hzs):
			if js: js = js[1:-1]
			l.append((hz, yb + c, js))
		return l

