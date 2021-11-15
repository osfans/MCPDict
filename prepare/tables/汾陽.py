#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_ll_fy"
	_file = "汾阳方言同音字表20211211.tsv"
	note = "版本：2021-12-21<br>來源：李衛鋒 2018《汾陽方言研究》；趙駿程 1963《汾陽話與普通話》；Hynuza 增補"
	tones = "324 1 1a 陰平 ꜀,22 2 1b 陽平 ꜁,312 3 2 上 ꜂,,55 5 3 去 ꜄,,2 7 4a 陰入 ꜆,312 8 4b 陽入 ꜇"
