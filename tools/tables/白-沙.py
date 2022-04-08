#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "och_ba"
	_file = "BaxterSagartOC2015-10-13.tsv"
	isYb = False

	def parse(self, fs):
		return fs[0], fs[4]
