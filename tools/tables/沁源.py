#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cjy_sd_qy"
	tones = "324 1 1a 陰平 ꜀,33 2 1b 陽平 ꜁,,,53 5 3 去 ꜄,,31 7 4 入 ꜆"
	simplified = 2

	def format(self, line):
		line = re.sub("^(.*?)\[", "\\1	[", line)
		return line
