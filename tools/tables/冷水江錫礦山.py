#!/usr/bin/env python3

from tables._湘 import 字表 as 表

class 字表(表):
	key = "hsn_ls_xh_lsjxks"
	_file = "冷水江锡矿山.tsv"
	
	def parse(self, fs):
		fs = fs[:4]
		del fs[2]
		return 表.parse(self, fs)
