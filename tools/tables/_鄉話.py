#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	note = "版本：2022-01-02<br>來源：楊蔚.湘西鄉話語音研究[M].廣東人民出版社,2010<br>轉錄者：跳跳老鼠"
	_file = "鄉話方言字表*.tsv"
	simplified = 2

	def parse(self, fs):
		hz, js = fs[:2]
		yb = fs[self.index] + fs[self.index+1]
		return hz, yb, js

