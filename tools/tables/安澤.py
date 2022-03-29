#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cjy_az"
	tones = "21 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,42 3 2 上 ꜂,,51 5 3 去 ꜄,,33 7 4 入 ꜆"
	_file = "安泽.tsv"
	simplified = 2
	
	def format(self, line):
		line = re.sub("^(.*?)［", "\\1	[", line)
		return line
