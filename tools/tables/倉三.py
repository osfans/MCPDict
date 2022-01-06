#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cj3"
	_file = "cj3.txt"
	_lang = "倉頡三代"
	note = "來源：https://github.com/Arthurmcarthur/Cangjie3-Plus"

	def parse(self, fs):
		return fs[-1], fs[0]
