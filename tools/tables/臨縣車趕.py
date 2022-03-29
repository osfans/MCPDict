#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_ll_fz_lxcg"
	_file = "临县车赶*.tsv"
	tones = "24 1 1a 陰平 ꜀,55 2 1b 陽平 ꜁,412 3 2 上 ꜂,,51 5 3 去 ꜄,,5 7 4a 陰入 ꜆,24 8 4b 陽入 ꜇"
	simplified = 2
