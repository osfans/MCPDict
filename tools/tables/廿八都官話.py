#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_fyd_nbdgh"
	note = "符号说明：&韵母更倾向读“yɔʔ”，也可以读“yɐʔ/yeʔ”@韵母更倾向读“yɐʔ/yeʔ”，也可以读“yɔʔ”"
	tones = "33 1 1a 陰平 ꜀,311 2 1b 陽平 ꜁,304 3 2 上 ꜂,,4 5 3 去 ꜄,,2 7 4 入 ꜆"
	_file = "廿八都官话同音字表.tsv"
	simplified = 2

	def format(self, line):
		line = re.sub("([&@])(?!{)","{\\1}",line)
		line = line.replace("&{","{&").replace("@{","{@")
		return line
