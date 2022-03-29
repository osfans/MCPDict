#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_oj_cnpm"
	_file = "苍南蒲门瓯语方言岛*.tsv"
	tones = "44 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,45 3 2a 陰上 ꜂,24 4 2b 陽上 ꜃,42 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,323 7 4a 陰入 ꜆,212 8 4b 陽入 ꜇"
	toneValues = {'44':1,'31':2,'45':3,'24':4,'42':5,'22':6,'323':7,'212':8}

	def parse(self, fs):
		hz,yb,sd = fs[:3]
		if not yb: return
		sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb
