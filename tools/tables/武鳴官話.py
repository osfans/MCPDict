#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_xn_wmgh,yue_"
	_file = "武鸣官话同音字表20211009.tsv"
	note = "版本：2021-12-02<br>來源：<u>清竮塵</u>整理自陸淼焱《武鳴縣城官話調查報告》"
	tones = "33 1 1a 陰平 ꜀,21 2 1b 陽平 ꜁,55 3 2 上 ꜂,,24 5 3 去 ꜄,,55 7a 4a 高入 ꜆,21 7b 4b 低入 ꜀,35   借入調 "
	toneValues = {"7a":"7", "7b":"8"}
	simplified = 2

	def format(self, line):
		for i,j in self.toneValues.items():
			line = line.replace("[%s]" % i, "[%s]" % j)
		return line

