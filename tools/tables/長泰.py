#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "nan_zq_ct"
	_file = "长泰.tsv"
	toneValues = {"44":1, "24":2, "53":3, "21":5, "22":6, "31":7, "2":8}
	
	def parse(self, fs):
		_, sm, ym, sd, hz, js = fs[:6]
		if sd in self.toneValues:
			sd = str(self.toneValues[sd])
		else:
			sd = ""
		yb = sm + ym + sd
		return hz, yb, js
