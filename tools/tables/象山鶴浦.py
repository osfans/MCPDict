#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "wuu_th_yj_xshp"
	tones = "53 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,35 3 2 上 ꜂,,44 5 3a 陰去 ꜄,213 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,12 8 4b 陽入 ꜇"
	_file = "象山鹤浦.tsv"
	simplified = 2

	def format(self, line):
		line = re.sub("^(.*?)\[", "\\1	[", line)
		return line
