#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "czh_txcsh"
	_file = "屯溪船上话同音字表*.tsv"
	toneValues = {"1b": "11", "2b": "12", "3b": "13", "5b":"15", "6b":"16"}
	
	def format(self, line):
		for i,j in self.toneValues.items():
			line = line.replace("[%s]" % i, "[%s]" % j)
		return line
