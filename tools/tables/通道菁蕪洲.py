#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "hsn_ls_tdjwz"
	_file = "通道菁芜洲本地话.tsv"

	def format(self, line):
		line = re.sub("([&])(?!{)","{西官借詞}",line)
		line = line.replace("&{","{(西官借詞)")
		return line
