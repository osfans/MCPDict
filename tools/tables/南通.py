#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cmn_jh_tt_nt"
	_file = "南通.csv"
	note = "更新：2018-01-08<br>來源：<a href=http://nantonghua.net/archives/5127/南通话字音查询/>南通方言網</a>"
	tones = "21 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,55 3 2 上 ꜂,,42 5 3a 陰去 ꜄,213 6 3b 陽去 ꜅,42 7 4a 陰入 ꜆,55 8 4b 陽入 ꜇"
	site = "南通方言網"
	url = "http://nantonghua.net/search/index.php?hanzi=%s"
	simplified = 2

	def parse(self, fs):
		hz = fs[1][0]
		yb = fs[-6] + fs[-4]
		js = fs[-7]
		c = ""
		if '白读' in js:
			c = "-"
		elif '文读' in js:
			c = "="
		elif '又读' in js:
			c = "+"
		yb = yb + c
		return hz, yb
