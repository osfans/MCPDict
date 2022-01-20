#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_wt_pg"
	_file = "偏关话同音字表*.tsv"
	note = "來源：任衝 2018《山西偏關方言語音研究》；sainirwuu, Skanda, Hynuza 整理錄入"
	tones = "24 1 1a 陰平 ꜀,44 2 1b 陽平 ꜁,213 3 2 上 ꜂,,52 5 3 去 ꜄,,4 7 4 入 ꜆"
	simplified = 2
	
	def format(self, line):
		return line.replace("{：",'{')
