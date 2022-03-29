#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_jh_hc_scwfh"
	tones = "31 1 1a 陰平 ꜀,34 2 1b 陽平 ꜁,213 3 2 上 ꜂,,55 5 3 去 ꜄,,5 7a 4a 高入 ꜆,24 7b 4b 低入 ꜇"
	_file = "安徽省舒城万佛湖方言同音字表*.tsv"
	simplified = 2

	def format(self, line):
		return line.replace("7a","7").replace("7b","8")
