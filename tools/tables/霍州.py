#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_zho_fh_py_hz"
	_file = "霍州话同音字表*.tsv"
	
	def format(self, line):
		line = line.replace("0a", "7").replace("0b", "8")
		return line
