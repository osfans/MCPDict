#!/usr/bin/env python3

import re
from tables._表 import 表 as _表

class 表(_表):
	sms = None
	ym = None

	def 統(自, 行):
		return 行.lstrip("#")

	def 析(自, 列):
		if not 自.sms:
			自.sms = 列
			return
		if 列[0]:
			自.ym = 列[0]
		sd = 列[1]
		l = list()
		for i,sm in enumerate(自.sms):
			if not sm: continue
			yb = sm + 自.ym + sd
			for 字 in 列[i]:
				l.append((字, yb))
		return l
