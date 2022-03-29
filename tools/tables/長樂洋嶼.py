#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_fyd_clyy"
	tones = "21 1 1a 陰平 ꜀,51 2 1b 陽平 ꜁,55 3 2 上 ꜂,,31 5 3 去 ꜄"
	_file = "长乐洋屿.tsv"
	simplified = 2

	def format(self, line):
		line = re.sub("^(.*?)［", "\\1	[", line).replace("", "Ø")
		return line
