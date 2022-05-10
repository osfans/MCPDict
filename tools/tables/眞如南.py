#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	def parse(self, fs):
		_, _, hz, js, sm, ym, sd = fs[:7]
		if sd == "0": sd = ""
		if sm == "Ø": sm = ""
		yb = sm + ym + sd
		return hz, yb, js
