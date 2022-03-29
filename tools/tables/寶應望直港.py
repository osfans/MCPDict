#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_jh_hc_bywzg"
	tones = "42 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,31 3 2 上 ꜂,,55 5 3 去 ꜄,,24 7 4 入 ꜆"
	_file = "宝应望直港.tsv"
	
	def format(self, line):
		line = re.sub("^(.*?) ?\[", "\\1	[", line)
		return line
