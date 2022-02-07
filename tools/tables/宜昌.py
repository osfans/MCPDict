#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_xn_hg_ez_yc"
	_file = "湖北省宜昌市方言同音字表*.tsv"
	note = "來源：<u>峽江流月</u>整理自劉興策《宜昌方言研究研究》"
	tones = "35 1 1a 陰平 ꜀,212 2 1b 陽平 ꜁,32 3 2 上 ꜂,24 5 3 去 ꜄"
	simplified = 2

	def format(self, line):
		if "调值" in line: return ""
		line = line.replace('""	"', '"#').replace('"零"', '""')\
				.replace("①","[1]").replace("②","[2]").replace("③","[3]").replace("④","[4]")\
				.replace("（","｛").replace("）","｝")
		return line

