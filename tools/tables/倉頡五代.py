#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	_file = "Cangjie5.tsv"
	short = "倉五"
	note = "來源：https://github.com/Jackchows/Cangjie5"

	def parse(self, fs):
		if len(fs) < 2: return
		return fs[0], fs[1]
