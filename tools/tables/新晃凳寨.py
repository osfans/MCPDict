#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cmn_xn_hg_xhdz"
	_file = "湖广新晃凳寨字表*.tsv"
	tones = "13 1 1a 陰平 ꜀,11 2 1b 陽平 ꜁,21 3 2 上 ꜂,,45 5 3 去 ꜄"
	toneValues = {"13":1,"11":2,"21":3,"45":5}

	def parse(self, fs):
		hz,_,_,_,_,sd,_,yb,js = fs[:9]
		sd = str(self.toneValues[sd])
		yb = yb.rstrip("012345") + sd
		return hz, yb, js
