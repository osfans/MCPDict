#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "hsn_ls_qlcl"
	note = "來源：吴伟军.贵州晴隆长流“喇叭苗”土话音系[J].方言,2019(4):446-462."
	tones = "44 1 1a 陰平 ꜀,21 2 1b 陽平 ꜁,41 3 2 上 ꜂,,35 5 3 去 ꜄,,13 7 4 入 ꜆"
	_file = "晴隆長流喇叭苗話.tsv"
	disorder = True
	simplified = 2

	def parse(self, fs):
		_, yb, sd, hzs = fs[:4]
		yb = yb + sd
		l = list()
		for hz, c, js in re.findall("(.)([-=]?)(\[.*?\[.*?\].*?\]|\[.*?\])?", hzs):
			if js: js = js[1:-1]
			l.append((hz, yb + c, js))
		return l

