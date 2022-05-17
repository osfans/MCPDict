#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

#http://humanum.arts.cuhk.edu.hk/Lexis/lexi-mf/dialectIndex.php?point=A
class 表(_表):
	site = "漢語多功能字庫"
	url = "http://humanum.arts.cuhk.edu.hk/Lexis/lexi-mf/dialect.php?word=%s"
	toneNames = None

	def parse(self, fs):
		sy, sd, hzs = fs[1:4]
		dm = sd.rstrip("012345")
		sd = sd[len(dm):]
		yb = sy.strip() + (self.toneMaps[sd] if sd else "")
		l = list()
		for hz in hzs.split(","):
			hz = hz.strip()
			if not hz: continue
			l.append((hz, yb))
		return l
