#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_tz_xj"
	_file = "仙居方言字表*.tsv"
	note = "更新：2021-08-25<br>來源：<u>落橙</u>"
	tones = "334 1 1a 陰平 ꜀,312 2 1b 陽平 ꜁,423 3 2 上 ꜂,,55 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"
	toneValues = {'334':1,'312':2,'423':3,'55':5,'22':6,'5':7,'2':8}

	def parse(self, fs):
		hz,_,yb,sd,js = fs[:5]
		sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb, js
