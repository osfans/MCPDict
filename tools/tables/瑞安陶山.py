#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_oj_rats"
	_file = "浙南瓯语(颜逸明)-瑞安陶山-字表-横.tsv"

	def parse(self, fs):
		hz, sm, ym, sd, js, bz = fs[:6]
		sd = sd.strip("[]")
		yb = sm + ym + sd
		js = (js + " " +bz).strip()
		return hz, yb, js

