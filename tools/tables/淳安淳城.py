#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "czh_yz_cacc"
	tones = "224 1 1a 陰平 ꜀,445 2 1b 陽平 ꜁,55 3 2 上 ꜂,,,22 6 3 去 ꜄,5 7 4a 陰入 ꜆,13 8 4b 陽入 ꜇"
	_file = "淳安淳城同音字表*.tsv"
	simplified = 2
