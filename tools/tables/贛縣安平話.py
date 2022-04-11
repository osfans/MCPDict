#!/usr/bin/env python3

from tables._縣志 import 表 as _表
import re

class 表(_表):

	def format(self, line):
		line = line.replace("：[", "	[").replace("1a", "1").replace("1b", "9")
		return line
