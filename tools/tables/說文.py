#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	full = "說文解字"
	short = "說文"
	site = "說文解字線上搜索"
	url = "http://www.shuowen.org/?kaishu=%s"
	note = "來源：<a href=https://github.com/shuowenjiezi/shuowen/>說文解字網站數據</a>"
	ybTrimSpace = False
	dictionary = True
	
	def parse(self, fs):
		fq = fs[1].split(" ")[0]
		return fs[0], fq, "\t".join(fs[2:])
