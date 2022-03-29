#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_zho_fh_py_gxyd"
	_file = "古县尧店同音字表*.tsv"
	tones = "21 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,53 3 2 上 ꜂,,33 5 3 去 ꜄"
	simplified = 2
