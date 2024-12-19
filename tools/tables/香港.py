#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables import hex2chr

class 表(_表):
	site = "粵語審音配詞字庫"
	url = "http://humanum.arts.cuhk.edu.hk/Lexis/lexi-can/search.php?q=%3$s"
	patches = {"㛟": "wun6", "𡃜": "kok3", "鿽": "zaa3"}
	isYb = False

	def parse(self, fs):
		if len(fs) < 2: return
		hz, py = fs[:2]
		py = py.replace(" ", "-")
		return hz, py

	def patch(self, d):
		for line in open(self.fullname("Unihan_Readings.txt"),encoding="U8"):
			line = line.strip()
			if not line.startswith("U"): continue
			fields = line.strip().split("\t", 2)
			hz, typ, yin = fields
			if typ == "kCantonese":
				yin = yin.strip().split(" ")
				hz = hex2chr(hz)
				for y in yin:
					if y not in d[hz]:
						d[hz].append(y)
		_表.patch(self, d)
