#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "nan_cs_yx"
	simplified = 2
	tones = "44 1 1a 陰平 ꜀,32 2 1b 陽平 ꜁,53 3 2 上 ꜂,,21 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,4 7 4a 陰入 ꜆,12 8 4b 陽入 ꜇"
	_file = "云霄话同音字表.tsv"

	def format(self,line):
		if line.startswith('""'): return ""
		line = line.replace("（","{").replace("）","}").replace("〉","}")
		return line
