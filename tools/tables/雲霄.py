#!/usr/bin/env python3

from tables._縣志 import 表 as _表

class 表(_表):

	def format(self,line):
		if line.startswith('""'): return ""
		line = line.replace("（","{").replace("）","}").replace("〉","}")
		return line
