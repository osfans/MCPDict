#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_bz_jxzl"
	_file = "介休张良同音字表*.tsv"
	note = "來源：楊紅 2017《介休市張良村方言語音研究》；Hynuza, Skanda 整理錄入"
	tones = "13 1 1 平 ꜀,,523 3 2 上 ꜂,,45 5 3 去 ꜄,,13 7 4a 陰入 ꜆,523 8 4b 陽入 ꜇"
	simplified = 2
