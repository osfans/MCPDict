#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "nan_cs_fhxf"
	_file = "方言调查字表（閩-鳳凰-新豐）*.tsv"

	def parse(self, fs):
		hz,_,py,yb,js = fs[:5]
		sd = py[-1]
		sd = ord(sd) - ord("①") + 1
		yb = yb.rstrip("˩˨˧˦˥0") + str(sd)
		return hz, yb, js
