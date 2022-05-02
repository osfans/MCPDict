#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	toneValues = {'33':1,'31':2,'42':3,'55':5,'13':6,'5':7,'2':8}

	def parse(self, fs):
		hz,_,yb,sd,js = fs[:5]
		if len(hz) != 1: return
		sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb, js
