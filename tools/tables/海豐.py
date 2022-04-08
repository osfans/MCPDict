#!/usr/bin/env python3

import re
from tables._表 import 表

class 字表(表):
	key = "nan_cs_hf"
	_file = "海丰字表*.tsv"
	toneValues = {'阴平':1,'阳平':2,'阴上':3,'阳上':4,'阴去':5,'阳去':6,'阴入':7,'阳入':8}

	def parse(self, fs):
		l = list()
		hz,wds,bds,js = fs[:4]
		if not hz: return
		hz = hz[0]
		yd = len(bds) > 0 and len(wds) > 0
		if js:
			for i in self.toneValues:
				js = js.replace(i, str(self.toneValues[i]))
		for ybs in (bds, wds):
			if not ybs: continue
			for yb in ybs.split("，"):
				if "（" in yb:
					ybzs = re.findall("^(.*?)（(.*?)）$", yb)
					yb = ybzs[0][0]
					c = ybzs[0][1]
				for i in self.toneValues:
					yb = yb.replace(i, str(self.toneValues[i]))
				if yd:
					c = '-' if ybs == bds else '='
					yb = yb + c
				if yb.startswith("["):
					js += yb[:3]
					yb = yb[3:]
				if "训" in yb:
					yb = yb.replace("训", "")
					js = "训" + js
				l.append((hz, yb, js))
		return l
