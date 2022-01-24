#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "yue_gf_nsnt"
	_file = "南头粤语.tsv"
	note = "來源：陈熙元整理自《南頭方言志》"
	tones = "24 1 1a 上陰平 ꜀,44 2 1c 陽平 ꜁,35 3 2a 陰上 ꜂,13 4 2b 陽上 ꜃,33 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇,55 1 1b 下陰平 ꜆"
	
	def parse(self, fs):
		if len(fs) < 6: return
		hz, py, yb, js = fs[0], fs[3], fs[4], fs[5]
		sd = py[-1]
		if sd == "h": sd = "9"
		yb = yb.rstrip("¹²³⁴⁵") + sd
		return hz, yb, js
