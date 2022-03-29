#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "gan_dt_cbss"
	tones = "445 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,31 3 2 上 ꜂,,213 5 3a 陰去 ꜄,33 6 3b 陽去 ꜅,45 7 4 入 ꜆"
	_file = "赤壁神山同音字表.tsv"
	simplified = 2
	
	def format(self, line):
		return line.replace("", "ᵑ")

