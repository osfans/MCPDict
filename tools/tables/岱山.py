#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_th_yj_ds"
	tones = "53 1 1a 陰平 ꜀,223 2 1b 陽平 ꜁,433 3 2 上 ꜂,,44 5 3a 陰去 ꜄,13 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,12 8 4b 陽入 ꜇"
	toneValues = {"53":1, "223":2, "433":3, "44":5, "13":6, "5":7, "12":8}

	def parse(self, fs):
		hz,_,yb,js,sm,ym,sd = fs[:7]
		if not yb: return
		if sd == "0": sd = ""
		else: sd = str(self.toneValues[sd])
		yb = sm + ym + sd
		return hz, yb, js
