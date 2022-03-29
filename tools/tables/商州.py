#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_zho_gz_sz"
	tones = "31 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,53 3 2 上 ꜂,,44 5 3 去 ꜄"
	simplified = 2

	def format(self, line):
		if "#" in line: return "#"
		line = re.sub("^(.*?)\[", "\\1	[", line)
		return line
