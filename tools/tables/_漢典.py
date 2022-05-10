#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	def parse(self, fs):
		if len(fs) > 9:
			hz,_,_,_,_,sms,yms,dzs,_,js = fs[:10]
		else:
			hz,_,_,_,sms,yms,dzs,_,js = fs[:9]
		if dzs.startswith("调"): return
		l = list()
		js = js.replace("<br>", " ")
		for sm in sms.split("/"):
			for ym in yms.split("/"):
				for dz in dzs.split("/"):
					yb = self.dz2dl(sm + ym, dz)
					l.append((hz, yb, js))
					js = ""
		return l
