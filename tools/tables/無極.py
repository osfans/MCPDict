#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_wj"
	tones = "31 1 1a 陰平 ꜀,213 2 1b 陽平 ꜁,35 3 2 上 ꜂,,51 5 3a 陰去 ꜄,353 6 3b 陽去 ꜅"
	_file = "无极.tsv"
	simplified = 2
