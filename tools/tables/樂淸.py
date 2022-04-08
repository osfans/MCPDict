#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_oj_yq"
	_file = "乐清2.0.tsv"

	def parse(self, fs):
		_id,sm,ym,sd,hz,js = fs[:6]
		if sm == "〇": sm = ""
		yb = sm + ym + sd
		return hz, yb, js

