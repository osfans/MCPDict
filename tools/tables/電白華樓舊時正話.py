#!/usr/bin/env python3

from tables._縣志 import 表 as _表

class 表(_表):
	toneValues = {"7a":"7", "7b":"8"}

	def format(self, line):
		for i,j in self.toneValues.items():
			line = line.replace("[%s]" % i, "[%s]" % j)
		return line
