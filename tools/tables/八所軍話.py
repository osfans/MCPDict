#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_fyd_bsjh"
	_file = "《八所军话音字表》*.tsv"
	_lang = "東方八所軍話"
	note = "來源：<u>一冂水</u>整理自《海南東方八所軍話音系》"
	tones = "33 1 1a 陰平 ꜀,21 2 1b 陽平 ꜁,51 3 2 上 ꜂,,24 5 3 去 ꜄,,3 7 4 入 ꜆"
	simplified = 2

