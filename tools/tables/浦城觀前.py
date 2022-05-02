#!/usr/bin/env python3

from tables._縣志 import 表 as _表
import re

class 表(_表):

	def format(self, line):
		line = line.replace("", "Ø").replace("", "")
		line = re.sub("^(.*?)［", "\\1	［", line)
		return line
