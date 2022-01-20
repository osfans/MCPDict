#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_bz_wskz"
	_file = "文水开栅同音字表*.tsv"
	note = "來源：韓昆瑜 2020《山西文水開柵方言語音研究》；Hynuza, Skanda 整理錄入"
	tones = "22 1 1 平 ꜀,,312 3 2 上 ꜂,,45 5 3 去 ꜄,,3 7 4a 陰入 ꜆,423 8 4b 陽入 ꜇"
	simplified = 2
