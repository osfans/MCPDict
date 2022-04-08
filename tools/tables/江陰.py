#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_th_pl_jy"
	_file = "江阴jiangyin.tsv"

	def parse(self, fs):
		return fs[3], fs[6]
