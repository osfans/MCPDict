#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_sl_tsly"
	_file = "泰顺罗阳同音字表*.tsv"
	toneValues = {'224':1,'42':2,'51':3,'21':4,'35':5,'11':6,'5':7,'1':8, '33':9}

	def parse(self, fs):
		hz,_,yb,sd,js = fs[:5]
		if not yb: return
		if sd == "0": sd = ""
		else: sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb, js
