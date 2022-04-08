#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cjy_bz_jxzlan"
	_file = "介休张兰镇同音字表*.tsv"
	
	def format(self, line):
		line = re.sub("[\[［](\d)[\]］][）)]","\\1)",line)
		return line
