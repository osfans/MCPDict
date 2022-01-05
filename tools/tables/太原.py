#!/usr/bin/env python3

from tables._音節表 import 字表 as 表

class 字表(表):
	key = "cjy_bz_ty"
	note = "更新：2021-11-12<br>來源：http://humanum.arts.cuhk.edu.hk/Lexis/lexi-mf/dialectIndex.php?point=E"
	tones = "11 1 1 平 ꜀,,53 3 2 上 ꜂,,45 5 3 去 ꜄,,2 7 4a 陰入 ꜆,54 8 4b 陽入 ꜇"
	toneNames = {"平聲11": 1, "上聲53": 3, "去聲45": 5, "陰入2": 7, "陽入54": 8}
