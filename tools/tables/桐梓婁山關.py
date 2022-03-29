#!/usr/bin/env python3

from tables._希 import 字表 as 表

class 字表(表):
	key = "cmn_xn_xs_mc_tzlsg"
	tones = "35 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,42 3 2 上 ꜂,,13 5 3 去 ꜄,,3 7 4 入 ꜆"
	_file = "*夜郎今言*.tsv"
	toneValues = {"˧˥": 1, "˧˩": 2, "˦˨": 3, "˩˧": 5, "˧": 7, "": ""}

