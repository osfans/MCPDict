#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "gan_dt_cbss"
	_file = "赤壁神山同音字表.tsv"
	
	def format(self, line):
		return line.replace("", "ᵑ")

