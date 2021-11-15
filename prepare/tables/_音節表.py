#!/usr/bin/env python3

from tables._表 import 表

#http://humanum.arts.cuhk.edu.hk/Lexis/lexi-mf/dialectIndex.php?point=A
class 字表(表):
	site = "漢語多功能字庫"
	url = "http://humanum.arts.cuhk.edu.hk/Lexis/lexi-mf/dialect.php?word=%s"
	toneNames = None

	def parse(self, fs):
		sy, sd, hzs = fs[1:4]
		if hzs == "字元": return
		yb = sy + str(self.toneNames[sd])
		l = list()
		for hz in hzs.split(","):
			if not hz: continue
			l.append((hz, yb))
		return l
