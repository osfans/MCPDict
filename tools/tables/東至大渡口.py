#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_jh_aq_dzddk"
	tones = "31 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,213 3 2 上 ꜂,,51 5 3 去 ꜄,,55 7 4 入 ꜆"
	_file = "安徽省东至大渡口方言同音字表*.tsv"
	simplified = 2
