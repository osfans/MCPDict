#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_jh_hx_aq_tcn"
	tones = "31 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,213 3 2 上 ꜂,,53 5 3a 陰去 ꜄,42 6 3b 陽去 ꜅,5 7 4 入 ꜆"
	_file = "安徽省桐城南部方言同音字表*.tsv"
	simplified = 2
