#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "nan_cs_yx"
	_file = "云霄话同音字表*.tsv"

	def format(self,line):
		if line.startswith('""'): return ""
		line = line.replace("（","{").replace("）","}").replace("〉","}")
		return line
