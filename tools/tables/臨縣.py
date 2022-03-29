#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_ll_fz_lx"
	_file = "临县县城话同音字表*.tsv"
	tones = "24 1 1a 陰平 ꜀,44 2 1b 陽平 ꜁,312 3 2 上 ꜂,,52 5 3 去 ꜄,,44 7 4a 陰入 ꜆,24 8 4b 陽入 ꜇"
	simplified = 2
