#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "hsn_ls_tdjwz"
	tones = "45 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,33 3 2a 陰上 ꜂,11 4 2b 陽上 ꜃,44 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅"
	_file = "通道菁芜洲本地话.tsv"
	simplified = 2

	def format(self, line):
		line = re.sub("([&])(?!{)","{西官借詞}",line)
		line = line.replace("&{","{(西官借詞)")
		return line
