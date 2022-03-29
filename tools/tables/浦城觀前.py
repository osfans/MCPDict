#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "mnp_jo_pcgq"
	tones = "44 1 1a 陰平 ꜀,11 2 1b 陽平 ꜁,113 3 2a 陰上 ꜂,55 4 2b 陽上 ꜃,53 5 3a 陰去 ꜄,31 6 3b 陽去 ꜅,25 7 4 入 ꜆"
	_file = "浦城观前.tsv"
	simplified = 2

	def format(self, line):
		line = line.replace("［", "[").replace("］","]").replace("", "Ø").replace("", "")
		line = re.sub("^(.*?) ?\[", "\\1	[", line)
		return line
