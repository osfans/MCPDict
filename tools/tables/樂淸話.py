#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		_id,sm,ym,sd,hz,js = fs[:6]
		if sd == "调": return
		if sm == "〇": sm = ""
		yb = sm + ym + sd
		return hz, yb, js

