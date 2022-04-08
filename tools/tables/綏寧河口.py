#!/usr/bin/env python3

from tables._湘 import 字表 as 表

class 字表(表):
	key = "hsn_ls_sh_snhk"
	_file = "绥宁河口.tsv"

	def parse(self, fs):
		del fs[2]
		return 表.parse(self, fs)
