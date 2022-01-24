#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_sd_cy_tl"
	_file = "屯留话同音字表*.tsv"
	note = "來源：張長江 2020《山西屯留方言語音研究》；Hynuza, Skanda 整理錄入"
	tones = "31 1 1a 陰平 ꜀,11 2 1b 陽平 ꜁,43 3 2 上 ꜂,,53 5 3 去 ꜄,,1 7 4a 陰入 ꜆,54 8 4b 陽入 ꜇"
	simplified = 2
