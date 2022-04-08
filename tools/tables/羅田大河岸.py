#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_jh_hx_ltdha"
	_file = "罗田大河岸.tsv"

	def format(self, line):
		line = line.replace("[", "［").replace("", "Ø")
		line = re.sub("^(.*?)［", "\\1	［", line)
		return line
