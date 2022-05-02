#!/usr/bin/env python3

from tables._縣志 import 表 as _表

class 表(_表):
	
	def format(self, line):
		return line.replace("", "ᵑ")

