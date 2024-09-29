#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	def parse(self, fs):
		if " " not in fs[0]: return
		yb, hzs = fs[0].split(" ")[:2]
		yb = self.dz2dl(yb)
		l = list()
		for hz, js in re.findall(r"(.)(\{.*?\})?", hzs):
			l.append((hz, yb, js))
		return l
