#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "czh_yz_sasc"
	tones = "534 1 1a 陰平 ꜀,33 2 1b 陽平 ꜁,213 3 2a 陰上 ꜂,24 4 2b 陽上 ꜃,,52 6 3 去 ꜄,24 7 4 入 ꜆"
	_file = "遂安狮城同音字表*.tsv"
	simplified = 2
