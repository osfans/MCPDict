#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "czh_qw_qmrk"
	_file = "祁门箬坑同音字表*.tsv"
	note = "版本：2022-01-06<br>來源：王琳 2015 《祁門箬坑方言研究》；Її整理錄入"
	tones = "11 1 1a 陰平 ꜀,554 2 1b 陽平 ꜁,35 3 2 上 ꜂,,212 5 3a 陰去 ꜄,33 6 3b 陽去 ꜅,324 7 4 入 ꜆"
	simplified = 2
