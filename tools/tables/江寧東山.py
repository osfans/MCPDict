#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_jh_hc_jnds"
	tones = "31 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,22 3 2 上 ꜂,,44 5 3 去 ꜄,55 7 4 入 ꜆"
	_file = "江宁东山.tsv"
	simplified = 2

