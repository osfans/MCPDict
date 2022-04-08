#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_xn_wmgh"
	_file = "武鸣官话同音字表20211009.tsv"
	toneValues = {"7a":"7", "7b":"8"}

	def format(self, line):
		for i,j in self.toneValues.items():
			line = line.replace("[%s]" % i, "[%s]" % j)
		return line

