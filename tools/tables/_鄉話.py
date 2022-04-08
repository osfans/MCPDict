#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	_file = "鄉話*.tsv"

	def parse(self, fs):
		hz, js = fs[:2]
		yb = fs[self.index] + fs[self.index+1]
		return hz, yb, js

