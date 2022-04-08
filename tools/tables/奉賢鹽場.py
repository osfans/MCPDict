#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_th_yj_fxyc"
	_file = "奉贤盐场.tsv"
	toneValues = {'53':1,'334':3,'44':5,'113':6,'5':7,'12':8}

	def parse(self, fs):
		hz,_,yb,js,sm,ym,sd = fs[:7]
		if not yb: return
		if sd == "0": sd = ""
		else: sd = str(self.toneValues[sd])
		yb = sm + ym + sd
		return hz, yb, js
