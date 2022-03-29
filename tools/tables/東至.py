#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "gan_hy_dz"
	tones = "11 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,35 3 2 上 ꜂,,41 5 3 去 ꜄,,55 7 4 入 ꜆"
	_file = "安徽省东至方言同音字表*.tsv"
	simplified = 2
