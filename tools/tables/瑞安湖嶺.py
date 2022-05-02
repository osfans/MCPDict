#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		hz, sm, ym, sd, js = fs[1], fs[5], fs[6], fs[10], fs[4]
		if sd == "调类": return
		yb = sm + ym + sd
		return hz, yb, js

