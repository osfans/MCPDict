#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_zho_qagj"
	tones = "113 1 1 平 ꜀,,53 3 2 上 ꜂,,44 5 3 去 ꜄"
	_file = "秦安郭嘉同音字表*.tsv"
	simplified = 2
