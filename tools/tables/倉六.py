#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	_color = "#808080"
	_file = "cangjie6.dict.yaml"
	_lang = "倉頡六代"
	key = "cj6"
	note = "來源：https://github.com/LEOYoon-Tsaw/Cangjie6"

	def parse(self, fs):
		if len(fs) < 2: return
		return fs[0], fs[1]
