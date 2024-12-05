#!/usr/bin/env python3

from tables._音典 import 表 as _表
import re

class 表(_表):
	tones = None
	_file = "IDS.txt"
	sep = "\t"
	note = """IDS描述字符: ⿰⿱⿲⿳⿴⿵⿶⿷⿸⿹⿺⿻⿼⿽㇯⿾⿿〾？
來源: https://babelstone.co.uk/CJK/IDS.TXT
作者: Andrew West (魏安) <babelstone@gmail.com>
版本: 2024-07-28
"""
	pua = dict()

	def format(self, line):
		if line.startswith("#"):
			fs = line.split("\t")
			if len(fs) == 4 and fs[1].startswith("{"):
				self.pua[fs[1]] = re.findall(r"(.)\)", fs[2])[0]
		return line

	def parse(self, fs):
		#print(fs, len(fs))
		if len(fs) < 3: return
		hz = fs[1]
		ids = [i for i in fs[2:] if not i.startswith("*")]
		id = " ".join(ids).replace("^", "").replace("$", "")
		id = re.sub(r"\{[^\}]+\}", lambda x: self.pua.get(x.group(), x), id)
		return hz, id
