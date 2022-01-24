#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "hsn_ls_lyqth"
	note = "來源：陳暉.湘方言語音研究[M].湖南師範大學出版社.2006 陳暉.漣源方言研究[M].湖南教育出版社.1998<br>轉錄者：跳跳老鼠"
	tones = "44 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,42 3 2 上 ꜂,,45 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅,33 7 4 入 ꜆"
	_file = "漣源橋頭河*.tsv"
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

