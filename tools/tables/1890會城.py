#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		_,_,hzs,sm,ym = fs[:5]
		yb = sm + ym
		if not yb: return
		l = list()
		for hz in hzs:
			l.append((hz, yb))
		return l
