#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_fyd_qmjh"
	tones = "22 1 1a 陰平 ꜀,55 2 1b 陽平 ꜁,35 3 2 上 ꜂,,212 5 3 去 ꜄,,42 7 4 入 ꜆"
	_file = "祁门军话同音字表*.tsv"
	simplified = 2
