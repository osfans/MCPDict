#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_th_ls_fydzg"
	_file = "东梓关字表*.tsv"

	def format(self, line):
		return line.replace('�','□')

	def parse(self, fs):
		_, hz, js, yb, py = fs[:5]
		sd = py[-1]
		if not sd.isdigit() or sd == "0": sd = ""
		yb = yb.rstrip("¹²³⁴⁵")
		yb += sd
		return hz, yb, js
