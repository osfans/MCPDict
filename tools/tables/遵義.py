#!/usr/bin/env python3

from tables._希 import 字表 as 表

class 字表(表):
	key = "cmn_xn_xs_mc_zy"
	tones = "35 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,53 3 2 上 ꜂,,24 5 3 去 ꜄,,221 7 4 入 ꜆"
	_file = "*播州正韻*.tsv"
	toneValues = {"˧˥": 1, "˧˩": 2, "˥˧": 3, "˨˦": 5, "˨˨˩": 7, "": ""}

