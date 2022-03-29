#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_jh_hc_dt"
	tones = "21 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,212 3 2 上 ꜂,,55 5 3 去 ꜄,,5 7 4 入 ꜆"
	_file = "当涂.tsv"
	simplified = 2
