#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "wuu_th_yj_xshp"
	_file = "象山鹤浦.tsv"

	def format(self, line):
		line = re.sub("^(.*?)\[", "\\1	[", line)
		return line
