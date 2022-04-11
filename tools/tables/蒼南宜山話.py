#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		sm,ym,sd,hz,js = fs[:5]
		sd = sd.strip("[]")
		yb = sm + ym + sd
		js = js.strip("{}")
		return hz, yb, js
