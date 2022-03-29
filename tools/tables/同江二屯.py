#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_jl_tjet"
	tones = "213 1 1a 陰平 ꜀,52 2 1b 陽平 ꜁,45 3 2 上 ꜂,,31 5 3 去 ꜄"
	simplified = 2

	def format(self, line):
		line = re.sub("^(.*?)\[", "\\1	[", line)
		return line
