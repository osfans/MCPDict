#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "gan_hy_yx"
	_file = "安徽省岳西方言同音字表*.tsv"

	def format(self, line):
		line = line.replace("*", "□")
		return line
