#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_jh_hx_aq_zy"
	tones = "31 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,335 3 2 上 ꜂,,53 5 3 去 ꜄,,5 7 4 入 ꜆"
	_file = "安徽省枞阳方言同音字表*.tsv"
	simplified = 2
	
	def format(self, line):
		return line.replace("*", "□")
