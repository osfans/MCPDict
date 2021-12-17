#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "czh_qw_fljc"
	_file = "浮梁舊城同音字彙.tsv"
	note = "版本：2021-12-15<br>來源：Pekkhak（啵啵）整理自謝留文《江西浮樑（舊城村）方言同音字彙》"
	tones = "55 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,21 3 2 上 ꜂,,213 5 3a 陰去 ꜄,33 6 3b 陽去 ꜅"
	simplified = 2
