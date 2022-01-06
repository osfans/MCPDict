#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_bz_jx"
	_file = "介休话*.tsv"
	note = "版本：2022-01-06<br>來源：1991《介休方言志》張益梅。Vinchi 錄入增補。"
	tones = "13 1 1 平 ꜀,,523 3 2 上 ꜂,,45 5 3 去 ꜄,,13 7 4a 陰入 ꜆,523 8 4b 陽入 ꜇"
	simplified = 2
	
	def format(self, line):
		return line.replace("ø", "")
