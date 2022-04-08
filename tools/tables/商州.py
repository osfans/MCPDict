#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_zho_gz_sz"

	def format(self, line):
		if "#" in line: return "#"
		line = re.sub("^(.*?)\[", "\\1	[", line)
		return line
