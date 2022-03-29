#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "gan_hy_ss"
	tones = "213 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,42 3 2 上 ꜂,,21 5 3a 陰去 ꜄,24 6 3b 陽去 ꜅,5 7 4 入 ꜆"
	_file = "安徽宿松方言同音字表*.tsv"
	simplified = 2
