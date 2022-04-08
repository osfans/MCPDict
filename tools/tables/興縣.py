#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_ll_fz_xx"
	_file = "兴县话同音字表*.tsv"

	def format(self, line):
		return line.replace("ø","Ø")
