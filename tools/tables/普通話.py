#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	爲音 = False

	def 析(自, 列):
		if 列[0].startswith("#"): return
		l = list()
		字組,py = 列[:2]
		for 字 in 字組:
			for yb in py.split(","):
				js = ""
				if "{" in yb:
					yb, js = yb.split("{")
					js = js[:-1]
				l.append((字, yb, js))
		return l

