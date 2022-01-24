#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "hsn_cayi_ct_xxdt"
	note = "來源：黃靜.湖南省湘陰縣東塘話語音研究[D].長沙:湖南師範大學,2006<br>轉錄者：跳跳老鼠"
	tones = "44 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,42 3 2 上 ꜂,,45 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅"
	_file = "湘陰東塘*.tsv"
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

