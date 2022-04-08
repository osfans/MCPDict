#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	disorder = True

	def parse(self, fs):
		if len(fs) > 3:
			_, yb, sd, hzs = fs[:4]
		else:
			yb, sd, hzs = fs[:3]
		sd = sd.rstrip("a")
		if sd.endswith("b"): sd = "1" + sd[:-1]
		yb = yb + sd
		l = list()
		for hz, c, js in re.findall("(.)([-=]?)(\[[^[]]*?\[[^[]]*?\][^[]]*?\]|\[.*?\])?", hzs):
			if js: js = js[1:-1]
			if hz == "~": hz = "□"
			l.append((hz, yb + c, js))
		return l

