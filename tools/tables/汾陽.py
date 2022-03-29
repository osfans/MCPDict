#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_ll_fy"
	_file = "汾陽話*.tsv"
	tones = "324 1 1a 陰平 ꜀,22 2 1b 陽平 ꜁,312 3 2 上 ꜂,,55 5 3 去 ꜄,,2 7 4a 陰入 ꜆,312 8 4b 陽入 ꜇"
