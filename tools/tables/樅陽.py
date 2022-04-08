#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_jh_hx_aq_zy"
	_file = "安徽省枞阳方言同音字表*.tsv"
	
	def format(self, line):
		return line.replace("*", "□")
