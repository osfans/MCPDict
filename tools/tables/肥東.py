#!/usr/bin/env python3

import re
from tables._表 import 表

class 字表(表):
	key = "cmn_jh_hc_fdgc"
	_file = "肥東古城同音字表*.tsv"

	def parse(self, fs):
		if len(fs) < 2: return
		if fs[0].startswith("#"): return
		l = list()
		yb, hzs = fs
		yb = yb.rstrip("0")
		hzs = re.findall("(.)(\d)?\*?(\{.*?\})?", hzs)
		for hz,index,js in hzs:
			if js: js = js[1:-1]
			l.append((hz, yb, js))
		return l
