#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def format(self, line):
		return line.replace('�','□')

	def parse(self, fs):
		_, hz, js, yb, py = fs[:5]
		if yb == "IPA": return
		sd = py[-1]
		if not sd.isdigit() or sd == "0": sd = ""
		yb = yb.rstrip("¹²³⁴⁵")
		yb += sd
		return hz, yb, js
