#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_bz_wsph"
	_file = "文水裴会*.tsv"
	tones = "22 1 1 平 ꜀,,512 3 2 上 ꜂,,24 5 3 去 ꜄,,2 7 4a 陰入 ꜆,512 8 4b 陽入 ꜇"
	simplified = 2
