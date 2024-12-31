#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表 as _表

class 表(_表):
	def 析(自, 列):
		if "[" not in 列[0]: return
		l = list()
		行 = 列[0].replace(" ", "")
		for yb, 字組 in re.findall(r"\[(.*?)\]([^[]+)", 行):
			yb = 自.dz2dl(yb)
			for 字, js in re.findall("(.)(（.*?）)?", 字組):
				l.append((字, yb, js.strip("（）")))
		return l
