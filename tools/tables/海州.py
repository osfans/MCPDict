#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_jh_hc_hz"
	note = "版本：V1.1 (2021-10-24)<br>來源：<u>清竮塵</u>整理自蘇曉青《海州方言同音字彙》"
	tones = "214 1 1a 陰平 ꜀,324 2 1b 陽平 ꜁,41 3 2 上 ꜂,,55 5 3 去 ꜄,,24 7 4 入 ꜆"
	_file = "海州同音字表*.tsv"
	simplified = 2
