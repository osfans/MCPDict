#!/usr/bin/env python3

from tables._跳跳老鼠 import 表 as _表

class 表(_表):

	def parse(self, fs):
		del fs[2]
		return _表.parse(self, fs[:3])
