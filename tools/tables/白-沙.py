#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	isYb = False

	def parse(self, fs):
		return fs[0], fs[4]
