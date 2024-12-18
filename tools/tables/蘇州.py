#!/usr/bin/env python3

import re
from tables._表 import 表 as _表

class 表(_表):
	broaddict = dict()
	ybdict = dict()

	def initBroadDict(self):
		for line in open(self.get_fullname("苏州（记音替换版）1.1.tsv"), encoding="U8"):
			fs = line.split("\t")
			if len(fs) < 6: continue
			order, hz, sm, ym, sd, js = fs[:6]
			self.broaddict[order + hz] = sm + ym + sd

	def __init__(self):
		super().__init__()
		self.initBroadDict()

	def parse(self, fs):
		order, hz, sm, ym, sd, js = fs[:6]
		yb = sm + ym + sd
		broad = self.broaddict.get(order+hz, self.ybdict.get(yb, ""))
		if broad and broad != yb.replace("*", ""):
			self.ybdict[yb] = broad
			yb = yb + "/" + broad
		return hz, yb, js

