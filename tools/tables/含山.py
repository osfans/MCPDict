#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_jh_hc_ch_ch"
	tones = "31 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,211 3 2 上 ꜂,554 5 3 去 ꜄,5 7 4 入 ꜆"
	_file = "含山方言同音字表*.tsv"
	simplified = 2

	def format(self, line):
		if "调值" in line: return ""
		line = line.replace('""	"', '"#').replace('"零"', '""').replace("ø","Ø")\
				.replace("①","[1]").replace("②","[2]").replace("③","[3]").replace("④","[4]").replace("⑤","[5]")\
				.replace("（0）","[0]").replace(")","）").replace("（","｛").replace("）","｝")
		return line

