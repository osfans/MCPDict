#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_ll_zy"
	_file = "中阳话*.tsv"
	note = "版本：2021-12-31<br>來源：高曉慧 2019《山西中陽方言語音研究》；Skanda, Hynuza 整理錄入"
	tones = "24 1 1a 陰平 ꜀,33 2 1b 陽平 ꜁,423 3 2 上 ꜂,,53 5 3 去 ꜄,,33 7 4a 陰入 ꜆,423 8 4b 陽入 ꜇"
	simplified = 2
	
	def format(self, line):
		return line.replace("[",'“').replace("]","”")
