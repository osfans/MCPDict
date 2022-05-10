#!/usr/bin/env python3

import re
from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		l = list()
		hz,bds,wds,js = fs[:4]
		hz = hz[0]
		yd = len(bds) > 0 and len(wds) > 0
		for ybs in (bds, wds):
			if not ybs: continue
			for yb in ybs.split("，"):
				if "（" in yb:
					ybzs = re.findall("^(.*?)（(.*?)）$", yb)
					yb = ybzs[0][0]
					c = ybzs[0][1]
				yb = self.dz2dl(yb)
				if yd:
					c = '-' if ybs == bds else '='
					yb = yb + c
				l.append((hz, yb, js))
		return l
