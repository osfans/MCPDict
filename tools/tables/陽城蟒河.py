#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_hx_hj_ycmh"
	_file = "阳城蟒河同音字表*.tsv"
	tones = "213 1 1a 陰平 ꜀,33 2 1b 陽平 ꜁,42 3 2 上 ꜂,,53 5 3 去 ꜄,,22 7 4 入 ꜆"
	simplified = 2
