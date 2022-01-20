#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_fyd_hlzh,yue_"
	_file = "华楼正话同音字表20211129.tsv"
	note = "來源：<u>清竮塵</u>整理自陳雲龍《廣東電白舊時正話》"
	tones = "33 1 1a 陰平 ꜀,213 2 1b 陽平 ꜁,31 3 2 上 ꜂,,55 5 3 去 ꜄,,5 7a 4a 短入 ꜆,214 7b 4b 長入 ꜀"
	toneValues = {"7a":"7", "7b":"8"}
	simplified = 2

	def format(self, line):
		for i,j in self.toneValues.items():
			line = line.replace("[%s]" % i, "[%s]" % j)
		return line
