#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cdo_cnqk"
	_file = "钱库蛮话*.tsv"

	def parse(self, fs):
		sm,ym,sd,hz,js = fs[:5]
		if sd == "轻声": sd = ""
		yb = sm + ym + sd
		return hz, yb, js
