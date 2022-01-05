#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cjy_bz_py"
	_file = "平遥话同音字表*.tsv"
	note = "版本：2021-12-31<br>來源：喬全生 陳麗 1999《平遙話音檔》；侯精一 1999《現代晉語的研究》；Skanda & Sun 增補修訂"
	tones = "22 1 1a 陰平 ꜀,22 2 1b 陽平 ꜁,512 3 2 上 ꜂,,24 5 3 去 ꜄,,22 7 4a 陰入 ꜆,512 8 4b 陽入 ꜇"
