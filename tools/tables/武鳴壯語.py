#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		hz, _, yb, js = fs[:4]
		yb = self.dz2dl(yb)
		return hz, yb, js
