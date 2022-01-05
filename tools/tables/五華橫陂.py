#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "hak_whhb"
	_lang = "五華橫陂客家話"
	_file = "五华横陂客家方言字表*.tsv"
	note = "更新：2021-09-27<br>來源：<u>阿纓</u>整理自魏宇文《五華方言同音字彙》"
	tones = "44 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,31 3 2 上 ꜂,,53 5 3 去 ꜄,,1 7 4a 陰入 ꜆,5 8 4b 陽入 ꜇"
	toneValues = {'44':1,'13':2,'31':3,'53':5,'1':7,'5':8}

	def parse(self, fs):
		hz,yb,sd,js = fs[:4]
		if not yb: return
		if sd == "0": sd = ""
		else: sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb, js
