#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_fyd_xfgh"
	tones = "33 1 1a 陰平 ꜀,52 2 1b 陽平 ꜁,31 3 2 上 ꜂,,412 5 3 去 ꜄,,54 7 4 入 ꜆"
	_file = "信丰官话同音字表*.tsv"
	simplified = 2
