#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	_file = "cangjie6.dict.yaml"
	short = "倉六"
	note = "來源：https://github.com/LEOYoon-Tsaw/Cangjie6"
	patches = {"□": "bu"}

	def format(self, line):
		if line.startswith("#") and "\t" in line: return line[1:]
		return line

	def parse(self, fs):
		if len(fs) < 2: return
		return fs[0], fs[1]
