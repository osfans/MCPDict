#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_oj_rahl"
	_file = "瑞安湖岭字表-20211211.tsv"

	def parse(self, fs):
		hz, sm, ym, sd, js = fs[1], fs[5], fs[6], fs[10], fs[4]
		yb = sm + ym + sd
		return hz, yb, js

