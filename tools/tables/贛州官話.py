#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_fyd_gzgh,hak_"
	note = "來源：<u>清竮塵</u>整理自鍾永超《贛南官話語音及其系屬考察》"
	tones = "33 1 1a 陰平 ꜀,42 2 1b 陽平 ꜁,452 3 2 上 ꜂,,212 5 3 去 ꜄,,5 7 4 入 ꜆"
	_file = "赣州官话同音字表*.tsv"
	simplified = 2
