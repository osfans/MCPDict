#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	def parse(self, fs):
		hz,_,yb,py,js = fs[:5]
		if not py: return
		sd = re.findall("[0-9][ABCDabcd]?$", py)
		if sd:
			sd = sd[0]
		else:
			sd = ""
		if sd == "0": sd = ""
		yb = yb.rstrip("0123456789¹²³⁴⁵") + sd
		js = js.rstrip('。')
		if hz == "？": hz = "□"
		return hz, yb, js
