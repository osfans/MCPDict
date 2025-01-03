#!/usr/bin/env python3

import re
from tables._表 import 表 as _表

class 表(_表):
	toneValues = {'阴平':1,'阳平':2,'阴上':3,'阳上':4,'阴去':5,'阳去':6,'阴入':7,'阳入':8}

	def 析(自, 列):
		l = list()
		字,wds,bds,js = 列[:4]
		if not 字: return
		字 = 字[0]
		yd = len(bds) > 0 and len(wds) > 0
		if js:
			for i in 自.toneValues:
				js = js.replace(i, str(自.toneValues[i]))
		for 音集 in (bds, wds):
			if not 音集: continue
			for yb in 音集.split("，"):
				if "（" in yb:
					ybzs = re.findall("^(.*?)（(.*?)）$", yb)
					yb = ybzs[0][0]
					c = ybzs[0][1]
				for i in 自.toneValues:
					yb = yb.replace(i, str(自.toneValues[i]))
				if yd:
					c = '-' if 音集 == bds else '='
					yb = yb + c
				if yb.startswith("["):
					js += yb[:3]
					yb = yb[3:]
				if "训" in yb:
					yb = yb.replace("训", "")
					js = "训" + js
				l.append((字, yb, js))
		return l
