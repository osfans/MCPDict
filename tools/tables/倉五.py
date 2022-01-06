#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cj5"
	_file = "Cangjie5.tsv"
	_lang = "倉頡五代"
	note = "來源：https://github.com/Jackchows/Cangjie5"

	def parse(self, fs):
		if len(fs) < 2: return
		return fs[0], fs[1]
