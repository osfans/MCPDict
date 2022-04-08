#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "xxx_zhuang"
	_file = "壮语汉字音字表*.tsv"
	toneValues = {"24":1, "31":2, "55":3, "42":4, "35":5, "33":6}
	
	def parse(self, fs):
		hz, py, yb, js = fs[:4]
		sy = yb.strip("123450")
		sd = yb[len(sy):]
		if sd in self.toneValues:
			sd = str(self.toneValues[sd])
		else:
			sd = ""
		if sy and sy[-1] in "ptk":
			if sd == "3": sd = "7"
			elif sd == "5": sd = "9"
			elif sd == "6": sd = "8"
		yb = sy + sd
		return hz, yb, js
