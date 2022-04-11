#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		sm,ym,sd,hz,js = fs[:5]
		if sd == "声调": return
		if sd == "轻声": sd = ""
		yb = sm + ym + sd
		return hz, yb, js
