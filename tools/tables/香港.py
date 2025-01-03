#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables import hex2chr

class 表(_表):
	網站 = "粵語審音配詞字庫"
	網址 = "http://humanum.arts.cuhk.edu.hk/Lexis/lexi-can/search.php?q=%3$s"
	補丁 = {"㛟": "wun6", "𡃜": "kok3", "鿽": "zaa3"}
	爲音 = False

	def 析(自, 列):
		if len(列) < 2: return
		字, py = 列[:2]
		py = py.replace(" ", "-")
		return 字, py

	def 修訂(自, d):
		for 行 in open(自.全路徑("Unihan_Readings.txt"),encoding="U8"):
			行 = 行.strip()
			if not 行.startswith("U"): continue
			fields = 行.strip().split("\t", 2)
			字, typ, yin = fields
			if typ == "kCantonese":
				yin = yin.strip().split(" ")
				字 = hex2chr(字)
				for y in yin:
					if y not in d[字]:
						d[字].append(y)
		_表.修訂(自, d)
