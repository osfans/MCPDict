#!/usr/bin/env python3

from tables._縣志 import 表 as _表
import re

class 表(_表):

	def format(self, line):
		line = re.sub("([&])(?!{)","{西官借詞}",line)
		line = line.replace("&{","{(西官借詞)")
		return line
