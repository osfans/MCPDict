#!/usr/bin/env python3

from tables._音典 import 表 as _表

class 表(_表):
	tones = None
	_file = "IDS.txt"
	sep = "\t"
	note = """IDS描述字符: ⿰⿱⿲⿳⿴⿵⿶⿷⿸⿹⿺⿻⿼⿽㇯⿾⿿〾？
來源: https://babelstone.co.uk/CJK/IDS.TXT
作者: Andrew West (魏安) <babelstone@gmail.com>
版本: 2024-07-28
"""

	def parse(self, fs):
		#print(fs, len(fs))
		if len(fs) < 3: return
		hz = fs[1]
		ids = [i for i in fs[2:] if not i.startswith("*")]
		id = " ".join(ids).replace("^", "").replace("$", "")
		return hz, id
