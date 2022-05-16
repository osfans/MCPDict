#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		hz,_,_,sm,ym,js = fs[:6]
		yb = sm + ym
		if not hz or not yb: return
		return hz, yb, js
