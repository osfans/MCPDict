#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "hsn_ls_ld"
	note = "來源：李济源,刘丽华,颜清徽.湖南娄底方言的同音字汇[J].方言,1987(4):294-305.<br>轉錄者：DaiDzao"
	tones = "44 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,42 3 2 上 ꜂,,35 5 3a 陰去 ꜄,11 6 3b 陽去 ꜅"
	_file = "婁底.tsv"
	disorder = True
	simplified = 2

	def parse(self, fs):
		_, yb, sd, hzs = fs[:4]
		yb = yb + sd
		l = list()
		for hz, c, js in re.findall("(.)([-=]?)(\[.*?\[.*?\].*?\]|\[.*?\])?", hzs):
			if js: js = js[1:-1]
			if hz == "~": hz = "□"
			l.append((hz, yb + c, js))
		return l

