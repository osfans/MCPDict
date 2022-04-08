#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cmn_xn_hg_xhdz"
	_file = "湖广新晃凳寨字表*.tsv"
	toneValues = {"13":1,"11":2,"21":3,"45":5}

	def parse(self, fs):
		hz,_,_,_,_,sd,_,yb,js = fs[:9]
		sd = str(self.toneValues[sd])
		yb = yb.rstrip("012345") + sd
		return hz, yb, js
