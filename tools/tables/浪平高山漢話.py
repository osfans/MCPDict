#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_xn_gshh"
	tones = "334 1 1a 陰平 ꜀,21 2 1b 陽平 ꜁,53 3 2 上 ꜂,,35 5 3 去 ꜄"
	_file = "高山汉话同音字表*.tsv"
	simplified = 2
