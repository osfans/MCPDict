#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_zho_fh_jz_yqxc"
	_file = "垣曲新城同音字表*.tsv"
	tones = "12 1 1 平 ꜀,,44 3 2 上 ꜂,,53 5 3 去 ꜄"
	simplified = 2
