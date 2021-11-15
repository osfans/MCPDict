#!/usr/bin/env python3

from tables._音節表 import 字表 as 表

class 字表(表):
	key = "cmn_jil_jn"
	note = "更新：2021-11-12<br>來源：http://humanum.arts.cuhk.edu.hk/Lexis/lexi-mf/dialectIndex.php?point=B"
	tones = "213 1 1a 陰平 ꜀,42 2 1b 陽平 ꜁,55 3 2 上 ꜂,,21 5 3 去 ꜄"
	toneNames = {"陰平213": 1, "陽平42": 2, "上聲55": 3, "去聲21": 5, "輕聲": ''}
