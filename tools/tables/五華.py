#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "hak_whsz"
	_file = "五华水寨客家话字表*.tsv"
	tones = "44 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,31 3 2 上 ꜂,,53 5 3 去 ꜄,,2 7 4a 陰入 ꜆,4 8 4b 陽入 ꜇"
	toneValues = {'44':1,'13':2,'31':3,'53':5,'2':7,'4':8}

	def parse(self, fs):
		yb,hz,js = fs[:3]
		if not yb: return
		sd = re.findall("\d+$", yb)[0]
		yb = yb[:-len(sd)]
		sd = str(self.toneValues.get(sd))
		yb += sd
		return hz, yb, js
