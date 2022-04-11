#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		return fs[3], fs[6]
