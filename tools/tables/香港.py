#!/usr/bin/env python3

from tables._表 import 表
from tables import hex2chr

class 字表(表):
	key = "yue_hk"
	_file = "jyut6ping3.dict.yaml"
	site = "粵語審音配詞字庫"
	url = "http://humanum.arts.cuhk.edu.hk/Lexis/lexi-can/search.php?q=%3$s"
	note = "來源：<a href=http://humanum.arts.cuhk.edu.hk/Lexis/lexi-can/>粵語審音配詞字庫</a>、<a href=http://www.unicode.org/charts/unihan.html>Unihan</a><br>說明：括號中的爲異讀讀音"
	tones = "55 1 1a 陰平 ꜀,35 3 2a 陰上 ꜂,33 5 3a 陰去 ꜄,11 2 1b 陽平 ꜁,23 4 2b 陽上 ꜃,22 6 3b 陽去 ꜅,55 7a 4a 上陰入 ꜆,33 7b 4b 下陰入 ꜀,22 8 4c 陽入 ꜇"
	patches = {"㛟": "wun6", "𡃜": "kok3", "鿽": "zaa3"}
	isYb = False

	def parse(self, fs):
		if len(fs) < 2: return
		hz, py = fs[:2]
		py = py.replace(" ", "-")
		return hz, py

	def patch(self, d):
		for line in open(self.get_fullname("Unihan_Readings.txt"),encoding="U8"):
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
		表.patch(self, d)
