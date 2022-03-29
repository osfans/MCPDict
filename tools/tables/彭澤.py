#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "gan_cd_pz"
	tones = "33 1 1a 陰平 ꜀,44 2 1b 陽平 ꜁,21 3 2 上 ꜂,,214 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅"
	_file = "江西省彭泽方言同音字表*.tsv"
	simplified = 2
