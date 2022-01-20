#!/usr/bin/env python3

from tables._音節表 import 字表 as 表

class 字表(表):
	key = "cmn_zho_xa"
	note = "來源：http://humanum.arts.cuhk.edu.hk/Lexis/lexi-mf/dialectIndex.php?point=A"
	tones = "21 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,53 3 2 上 ꜂,,44 5 3 去 ꜄"
	toneNames = {"陰平21": 1, "陽平24": 2, "上聲53": 3, "去聲44": 5}
