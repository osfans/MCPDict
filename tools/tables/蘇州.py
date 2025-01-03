#!/usr/bin/env python3

import re
from tables._表 import 表 as _表

class 表(_表):
	broaddict = dict()
	ybdict = dict()

	def initBroadDict(自):
		for 行 in open(自.全路徑("苏州（记音替换版）1.1.tsv"), encoding="U8"):
			列 = 行.split("\t")
			if len(列) < 6: continue
			order, 字, sm, ym, sd, js = 列[:6]
			自.broaddict[order + 字] = sm + ym + sd

	def __init__(自):
		super().__init__()
		自.initBroadDict()

	def 析(自, 列):
		order, 字, sm, ym, sd, js = 列[:6]
		yb = sm + ym + sd
		yb = yb.lstrip("*")
		broad = 自.broaddict.get(order+字, 自.ybdict.get(yb, ""))
		if broad and broad != yb:
			自.ybdict[yb] = broad
			yb = yb + "/" + broad
		return 字, yb, js

