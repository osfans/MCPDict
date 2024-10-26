#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	def parse(self, fs):
		if len(fs) < 2: return
		yb, hzs = fs[:2]
		if not yb: return
		l = list()
		for hz, js in re.findall(r"(.)(\{.*?\})?", hzs):
			if js: js = js[1:-1]
			l.append((hz, yb, js))
		return l
