#!/usr/bin/env python3

from tables._湘 import 字表 as 表

class 字表(表):
	key = "gan_ds_snmp"
	_file = "绥宁梅坪.tsv"
	
	def parse(self, fs):
		del fs[4], fs[2]
		return 表.parse(self, fs)
