#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_jh_aknt"
	simplified = 2
	tones = "22 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,55 3 2 上 ꜂,,31 5 3a 陰去 ꜄,33 6 3b 陽去 ꜅,214 7 4 入 ꜆"
	_file = "陕西省安康牛蹄方言同音字表.tsv"
