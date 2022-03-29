#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_oj_cnys"
	_file = "苍南宜山字表*.tsv"
	tones = "44 1 1a 陰平 ꜀,21 2 1b 陽平 ꜁,54 3 2a 陰上 ꜂,24 4 2b 陽上 ꜃,42 5 3a 陰去 ꜄,11 6 3b 陽去 ꜅,34 7 4a 陰入 ꜆,213 8 4b 陽入 ꜇"
	simplified = 2

	def parse(self, fs):
		sm,ym,sd,hz,js = fs[:5]
		sd = sd.strip("[]")
		yb = sm + ym + sd
		js = js.strip("{}")
		return hz, yb, js
