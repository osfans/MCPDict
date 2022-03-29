#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_bz_xy"
	_file = "孝义话*.tsv"
	tones = "11 1 1 平 ꜀,,312 3 2 上 ꜂,,53 5 3 去 ꜄,,2 7 4a 陰入 ꜆,312 8 4b 陽入 ꜇"
	simplified = 2
