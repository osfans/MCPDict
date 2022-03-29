#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "wuu_xz_sl_jxml"
	tones = "35 1 1 平 ꜀,,21 3 2a 陰上 ꜂,51 4 2b 陽上 ꜃,24 5 3 去 ꜄,,3 7 4a 陰入 ꜆,5 8 4b 陽入 ꜇"
	_file = "泾县茂林.tsv"
	simplified = 2

	def format(self, line):
		line = re.sub("^(.*?)\[", "\\1	[", line)
		return line
