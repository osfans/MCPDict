#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	sds = "平上去入"
	zsm = "並明定泥澄孃娘從邪崇俟常船群疑來匣日云以"
	def parse(self, fs):
		hzs = fs[9]
		yb = fs[13]
		sm = fs[2]
		if fs[6] not in self.sds:
			return
		sd = self.sds.index(fs[6]) * 2 + 1
		if sm in self.zsm: sd += 1
		yb = yb + str(sd)
		l = list()
		for hz in hzs:
			l.append((hz, yb))
		return l
