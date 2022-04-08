#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_oj_cnys"
	_file = "苍南宜山字表*.tsv"

	def parse(self, fs):
		sm,ym,sd,hz,js = fs[:5]
		sd = sd.strip("[]")
		yb = sm + ym + sd
		js = js.strip("{}")
		return hz, yb, js
