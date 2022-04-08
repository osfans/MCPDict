#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "nan_gxap"
	_file = "赣县安平.tsv"

	def format(self, line):
		line = line.replace("：[", "	[").replace("1a", "1").replace("1b", "9")
		return line
