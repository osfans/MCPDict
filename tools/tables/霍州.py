#!/usr/bin/env python3

from tables._縣志 import 表 as _表

class 表(_表):
	
	def format(self, line):
		line = line.replace("0a", "7").replace("0b", "8")
		return line
