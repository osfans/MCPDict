#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_jh_hx_ltdha"
	tones = "21 1 1a 陰平 ꜀,42 2 1b 陽平 ꜁,45 3 2 上 ꜂,,21 5 3a 陰去 ꜄,33 6 3b 陽去 ꜅,213 7 4 入 ꜆"
	_file = "罗田大河岸.tsv"
	simplified = 2

	def format(self, line):
		line = line.replace("[", "［").replace("", "Ø")
		line = re.sub("^(.*?)［", "\\1	［", line)
		return line
