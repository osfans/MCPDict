#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表 as _表

class 表(_表):
	def parse(self, fs):
		if "[" not in fs[0]: return
		l = list()
		line = fs[0].replace(" ", "")
		for yb, hzs in re.findall("\[(.*?)\]([^[]+)", line):
			yb = self.dz2dl(yb)
			for hz, js in re.findall("(.)(（.*?）)?", hzs):
				l.append((hz, yb, js.strip("（）")))
		return l
