#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_zho_ccwz"
	note = "來源：<u>清竮塵</u>整理自卜曉梅《陝西澄城（王莊鎮）方言同音字彙集》"
	tones = "21 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,53 3 2 上 ꜂,,44 5 3 去 ꜄"
	_file = "澄城王庄同音字表*.tsv"
	simplified = 2
