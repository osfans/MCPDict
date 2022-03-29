#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_ll_fz_xx"
	_file = "兴县话同音字表*.tsv"
	tones = "324 1 1a 陰平 ꜀,55 2 1b 陽平 ꜁,324 3 2 上 ꜂,,53 5 3 去 ꜄,,55 7 4a 陰入 ꜆,312 8 4b 陽入 ꜇"
	simplified = 2

	def format(self, line):
		return line.replace("ø","Ø")
