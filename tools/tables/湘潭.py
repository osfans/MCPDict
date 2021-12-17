#!/usr/bin/env python3

from tables._音節表 import 字表 as 表

class 字表(表):
	key = "hsn_xt"
	note = "更新：2021-11-12<br>來源：http://humanum.arts.cuhk.edu.hk/Lexis/lexi-mf/dialectIndex.php?point=G"
	tones = "33 1 1a 陰平 ꜀,12 2 1b 陽平 ꜁,42 3 2 上 ꜂,,55 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅,24 7 4 入 ꜆"
	toneNames = {"陰平33": 1, "陽平12": 2, "上聲42": 3, "陰去55": 5, "陽去21": 6, "入聲24": 7}
