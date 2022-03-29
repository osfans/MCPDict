#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "nan_qz_ct"
	_file = "长泰.tsv"
	tones = "44 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,53 3 2 上 ꜂,,21 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,31 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"
	toneValues = {"44":1, "24":2, "53":3, "21":5, "22":6, "31":7, "2":8}
	
	def parse(self, fs):
		_, sm, ym, sd, hz, js = fs[:6]
		if sd in self.toneValues:
			sd = str(self.toneValues[sd])
		else:
			sd = ""
		yb = sm + ym + sd
		return hz, yb, js
