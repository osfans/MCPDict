#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "yue_gf_baxx"
	_file = "深圳西乡粤语字表.tsv"
	note = "來源：王振辉整理自《深圳市志·社會風俗卷·方言志》"
	tones = "55 1 1a 陰平 ꜀,21 2 1b 陽平 ꜁,35 3 2a 陰上 ꜂,33 4 2b 陽上 ꜃,44 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,55 7 4a 陰入 ꜆,22 8 4b 陽入 ꜇"
	
	def parse(self, fs):
		if len(fs) < 9: return
		hz, sd, yb, js = fs[0], fs[4], fs[7], fs[8]
		yb = yb.rstrip("¹²³⁴⁵") + sd
		return hz, yb, js
