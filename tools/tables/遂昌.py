#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_sl_sc"
	_file = "吴语遂昌话字表*.tsv"
	note = "更新：2021-09-22<br>來源：<u>落橙</u>、<u>阿纓</u>"
	tones = "55 1 1a 陰平 ꜀,221 2 1b 陽平 ꜁,52 3 2a 陰上 ꜂,13 4 2b 陽上 ꜃,334 5 3a 陰去 ꜄,212 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,23 8 4b 陽入 ꜇"
	toneValues = {'55':1,'221':2,'52':3,'13':4,'334':5,'212':6,'5':7,'23':8}
	hasHead = False

	def parse(self, fs):
		hz,yb,sd,js = fs[:4]
		if not yb: return
		if sd == "0": sd = ""
		else: sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb, js
