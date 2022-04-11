#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	toneValues = {'44':1,'13':2,'31':3,'53':5,'2':7,'4':8}

	def parse(self, fs):
		yb,hz,js = fs[:3]
		if len(hz) != 1: return
		if not yb: return
		sd = re.findall("\d+$", yb)[0]
		yb = yb[:-len(sd)]
		sd = str(self.toneValues.get(sd))
		yb += sd
		return hz, yb, js
