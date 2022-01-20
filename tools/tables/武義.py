#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_jq_wy"
	note = "來源：<u>一三</u>"
	tones = "24 1 1a 陰平 ꜀,423 2 1b 陽平 ꜁,445 3 2a 陰上 ꜂,334 4 2b 陽上 ꜃,53 5 3a 陰去 ꜄,31 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,3 8 4b 陽入 ꜇"
	_file = "武義字表.tsv"

	def parse(self, fs):
		_,hz,py,yb,js = fs[:5]
		if not yb or len(hz)!=1: return
		sd = py[-1]
		if not sd.isdigit(): sd = ""
		yb = yb.rstrip("12345") + sd
		return hz, yb, js
