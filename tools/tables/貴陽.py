#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cmn_xn_cq_qz_gy"
	note = "來源：由築城燈神整理自《現代漢語方言音庫·貴陽話音檔》《現代漢語方言大詞典·分卷 貴陽方言詞典》《貴州省漢語方言特色詞語彙編》《新修支那省別志全志·第4卷 貴州省上》復旦大學中華文明數據中心"
	tones = "45 1 1a 陰平 ꜀,21 2 1b 陽平 ꜁,43 3 2 上 ꜂,24 5 3 去 ꜄"
	_file = "黔中筑韵*.tsv"
	
	def parse(self, fs):
		hz, _, py, yb, js = fs[:5]
		sd = py[-1]
		if not sd.isdigit(): sd = ""
		yb = yb.rstrip("˩˨˧˦˥") + sd
		return hz, yb, js

