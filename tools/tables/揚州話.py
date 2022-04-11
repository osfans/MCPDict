#!/usr/bin/env python3

import re
from tables._表 import 表 as _表

class 表(_表):
	simplified = 0

	def parse(self, fs):
		py,hzs = fs
		l = list()
		for c,hz,js in re.findall("([？#\+])?(.)(（[^）]*?（.*?）.*?）|（.*?）)?", hzs):
			if js: js = js[1:-1]
			p = ""
			if c == '+':
				p = "書"
				c = ""
			elif c == '？':
				c = "?"
			elif c == '#':
				c = "*"
			if p:
				js = f"({p}){js}"
			l.append((hz, py + c, js))
		return l
