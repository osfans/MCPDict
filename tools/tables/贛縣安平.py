#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "nan_gxap"
	tones = "44 1a 1a 陰平 ꜀,31 2 1b 陽平 ꜁,53 3 2 上 ꜂,,21 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,32 7 4a 陰入 ꜆,55 8 4b 陽入 ꜇,24 1b 1c 次陰平 ꜆"
	_file = "赣县安平.tsv"
	simplified = 2

	def format(self, line):
		line = line.replace("：[", "	[").replace("1a", "1").replace("1b", "9")
		return line
