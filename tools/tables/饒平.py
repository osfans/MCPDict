#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "nan_cs_rp"
	_file = "方言调查字表（闽-饶平）*.tsv"
	note = "版本：V2.6 (2022-01-06)<br>來源：<u>四方麻東</u>"
	tones = "33 1 1a 陰平 ꜀,55 2 1b 陽平 ꜁,51 3 2a 陰上 ꜂,25 4 2b 陽上 ꜃,212 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅,2 7 4a 陰入 ꜆,54 8 4b 陽入 ꜇"

	def parse(self, fs):
		hz,py,yb,js = fs[:4]
		if not yb: return
		sd = py[-1]
		if not sd.isdigit(): sd = ""
		yb = yb.rstrip("˩˨˧˦˥0") + sd
		return hz, yb, js
