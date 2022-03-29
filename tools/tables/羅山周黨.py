#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_zho_lszd"
	tones = "31 1 1a 陰平 ꜀,53 2 1b 陽平 ꜁,24 3 2 上 ꜂,,213 5 3 去 ꜄"
	_file = "罗山周党.tsv"
	simplified = 2

	def format(self, line):
		line = re.sub("^(.*?)\[", "\\1	[", line)
		return line
