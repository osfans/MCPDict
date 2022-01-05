#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cmn_xn_hg_xhdz"
	_file = "湖广新晃凳寨字表.tsv"
	note = "版本：2022-01-04<br>來源：<u>拜振华</u>"
	tones = "13 1 1a 陰平 ꜀,11 2 1b 陽平 ꜁,21 3 2 上 ꜂,,45 5 3 去 ꜄"

	def parse(self, fs):
		hz,_,py,yb,js = fs[:5]
		sd = py[-1]
		yb = yb.rstrip("012345") + sd
		return hz, yb, js
