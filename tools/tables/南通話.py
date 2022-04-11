#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	site = "南通方言網"
	url = "http://nantonghua.net/search/index.php?hanzi=%s"

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
