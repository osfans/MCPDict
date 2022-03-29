#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_xn_hg_xb_ty"
	tones = "35 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,21 3 2 上 ꜂,,13 5 3a 陰去 ꜄,33 6 3b 陽去 ꜅,55 7 4 入 ꜆"
	_file = "湖南省桃源方言同音字表*.tsv"
	simplified = 2

