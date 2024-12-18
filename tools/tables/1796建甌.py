#!/usr/bin/env python3

import re
from tables._表 import 表 as _表

class 表(_表):
	sms = None
	ym = None

	def format(self, line):
		return line.lstrip("#")

	def parse(self, fs):
		if not self.sms:
			self.sms = fs
			return
		if fs[0]:
			self.ym = fs[0]
		sd = fs[1]
		l = list()
		for i,sm in enumerate(self.sms):
			if not sm: continue
			yb = sm + self.ym + sd
			for hz in fs[i]:
				l.append((hz, yb))
		return l
