#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cjy_az"
	_file = "安泽.tsv"
	
	def format(self, line):
		line = re.sub("^(.*?)［", "\\1	［", line)
		return line
