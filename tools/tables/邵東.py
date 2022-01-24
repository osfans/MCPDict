#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "hsn_ls_sd"
	note = "來源：黃磊.邵東湘語語音研究[D].長沙:湖南師範大學,2005<br>轉錄者：DaiDzao"
	tones = "55 1 1a 陰平 ꜀,12 2 1b 陽平 ꜁,32 3 2 上 ꜂,,35 5 3a 陰去 ꜄,24 6 3b 陽去 ꜅"
	_file = "邵东同音字汇.tsv"
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

