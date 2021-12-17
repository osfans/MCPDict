#!/usr/bin/env python3

from tables._音節表 import 字表 as 表

class 字表(表):
	key = "mnp_jo"
	note = "更新：2021-11-12<br>來源：http://humanum.arts.cuhk.edu.hk/Lexis/lexi-mf/dialectIndex.php?point=Q"
	tones = "54 1 1 平 ꜀,,21 3 2 上 ꜂,,33 5 3a 陰去 ꜄,44 6 3b 陽去 ꜅,24 7 4a 陰入 ꜆,42 8 4b 陽入 ꜇"
	toneNames = {"平聲54": 1, "上聲21": 3, "陰去33": 5, "陽去44": 6, "陰入24": 7, "陽入42": 8}
