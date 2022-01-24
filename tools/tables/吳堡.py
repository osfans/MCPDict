#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_ll_fz_wb"
	_file = "吴堡话同音字表*.tsv"
	note = "來源：邢向東 王兆富 2014《吳堡方言調查研究》；灰原哀 木瓜 整理錄入"
	tones = "213 1 1a 陰平 ꜀,33 2 1b 陽平 ꜁,412 3 2 上 ꜂,,53 5 3 去 ꜄,,3 7 4a 陰入 ꜆,213 8 4b 陽入 ꜇"
	simplified = 2
