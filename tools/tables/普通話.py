#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	site = "字海"
	url = "http://yedict.com/zscontent.asp?uni=%2$s"
	isYb = False

	def parse(self, fs):
		if fs[0].startswith("#"): return
		l = list()
		hzs,py = fs[:2]
		for hz in hzs:
			for yb in py.split(","):
				js = ""
				if "{" in yb:
					yb, js = yb.split("{")
					js = js[:-1]
				l.append((hz, yb, js))
		return l

