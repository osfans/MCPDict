#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_zho_ccwz"
	tones = "21 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,53 3 2 上 ꜂,,44 5 3 去 ꜄"
	_file = "澄城王庄同音字表*.tsv"
	simplified = 2
