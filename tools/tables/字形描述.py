#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	tones = None
	_file = "IDS.txt"
	sep = "\t"
	說明 = """IDS描述字符: ⿰⿱⿲⿳⿴⿵⿶⿷⿸⿹⿺⿻⿼⿽㇯⿾⿿〾？
來源: https://babelstone.co.uk/CJK/IDS.TXT
作者: Andrew West (魏安) <babelstone@gmail.com>
版本: 2024-07-28
"""
	pua = dict()

	def 統(自, 行):
		if 行.startswith("#"):
			列 = 行.split("\t")
			if len(列) == 4 and 列[1].startswith("{"):
				自.pua[列[1]] = re.findall(r"(.)\)", 列[2])[0]
		return 行

	def 析(自, 列):
		#print(列, len(列))
		if len(列) < 3: return
		字 = 列[1]
		ids = [i for i in 列[2:] if not i.startswith("*")]
		id = " ".join(ids).replace("^", "").replace("$", "")
		id = re.sub(r"\{[^\}]+\}", lambda x: 自.pua.get(x.group(), x), id)
		return 字, id
