#!/usr/bin/env python3

from tables._音節表 import 字表 as 表

class 字表(表):
	key = "cmn_xn_hg_wh"
	note = "更新：2021-11-12<br>來源：http://humanum.arts.cuhk.edu.hk/Lexis/lexi-mf/dialectIndex.php?point=C"
	tones = "55 1 1a 陰平 ꜀,213 2 1b 陽平 ꜁,42 3 2 上 ꜂,,35 5 3 去 ꜄"
	toneNames = {"陰平55": 1, "陽平213": 2, "上聲42": 3, "去聲35": 5}
