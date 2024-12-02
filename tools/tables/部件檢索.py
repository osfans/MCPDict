#!/usr/bin/env python3

from tables._表 import 表 as _表
from collections import defaultdict
import re

class 表(_表):
	_file = "部件檢索.htm"
	short = "部件檢索"
	note = "來源：https://fgwang.blogspot.com/2023/10/unicode-151.html"
	patches = {"□": "!󰒂一", "〇": "@"}

	def update(self):
		d = defaultdict(list)
		f = open(self.spath,encoding="U8")
		cont = f.read()
		f.close()
		match = re.findall(r"var dt=\[.*?\]", cont, re.MULTILINE|re.DOTALL)[0]
		for line in match.split(","):
			line = line.strip('" ')
			d[line[0]].append(line[1:])
		self.write(d)

