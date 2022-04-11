#!/usr/bin/env python3

from tables._縣志 import 表 as _表

class 表(_表):
	toneValues = {"1b": "11", "2b": "12", "3b": "13", "5b":"15", "6b":"16"}
	
	def format(self, line):
		for i,j in self.toneValues.items():
			line = line.replace("[%s]" % i, "[%s]" % j)
		return line
