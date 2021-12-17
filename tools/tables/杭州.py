#!/usr/bin/env python3

from tables._音節表 import 字表 as 表

class 字表(表):
	key = "wuu_hz"
	note = "更新：2021-11-12<br>來源：http://humanum.arts.cuhk.edu.hk/Lexis/lexi-mf/dialectIndex.php?point=M"
	tones = "33 1 1a 陰平 ꜀,213 2 1b 陽平 ꜁,53 3 2 上 ꜂,,445 5 3a 陰去 ꜄,13 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"
	toneNames = {"陰平33": 1, "陽平213": 2, "陰上53": 3, "陰去445": 5, "陽去13": 6, "陰入5": 7, "陽入2": 8}
