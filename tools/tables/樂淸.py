#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_oj_yq"
	tones = "44 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,35 3 2a 陰上 ꜂,34 4 2b 陽上 ꜃,52 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,323 7 4a 陰入 ꜆,212 8 4b 陽入 ꜇"
	_file = "乐清2.0.tsv"
	simplified = 2

	def parse(self, fs):
		_id,sm,ym,sd,hz,js = fs[:6]
		if sm == "〇": sm = ""
		yb = sm + ym + sd
		return hz, yb, js

