#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_ll_jxjl"
	_file = "佳县佳芦*.tsv"
	note = "來源：張倩 2020《佳縣（佳蘆鎮）方言音系研究》；Skanda，Hynuza 整理錄入"
	tones = "213 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,534 3 2 上 ꜂,,42 5 3 去 ꜄,,4 7 4a 陰入 ꜆,213 8 4b 陽入 ꜇"
	simplified = 2
