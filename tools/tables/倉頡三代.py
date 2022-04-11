#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	_file = "cj3.txt"
	short = "倉三"
	note = "來源：https://github.com/Arthurmcarthur/Cangjie3-Plus"

	def parse(self, fs):
		return fs[-1], fs[0]
