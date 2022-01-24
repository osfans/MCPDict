#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "hsn_cayi_mlcl"
	note = "來源：陳山靑.湖南汨羅長樂方言音系[J].方言,2006(1):56-71.<br>轉錄者：跳跳老鼠"
	tones = "33 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,24 3 2 上 ꜂,,45 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅,43 7 4 入 ꜆"
	_file = "汨羅長樂*.tsv"
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

