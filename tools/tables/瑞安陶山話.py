#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		hz, sm, ym, sd, js, bz = fs[:6]
		sd = sd.strip("[]")
		yb = sm + ym + sd
		js = (js + " " +bz).strip()
		return hz, yb, js

