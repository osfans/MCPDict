#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "czh_yz_lxcsh"
	note = "更新：2021-10-06<br>來源：<u>清竮塵</u>整理自劉倩《九姓漁民方言研究》"
	tones = "53 1 1a 陰平 ꜀,33 2 1b 陽平 ꜁,214 3 2 上 ꜂,,44 5 3 去 ꜄,,55 7a 4a 上陰入 ꜆,12 8 4c 陽入 ꜇,5 7b 4b 下陰入 ꜀,,53 1b 1a 連讀降調 ꜀,,24 3b 2c 連讀升調 ꜂,,44 5b 3b 連讀高調 ꜄,,,12 8b 4d 連讀低調 ꜇"
	_file = "兰溪船上话同音字表*.tsv"
	toneValues = {"1b": "11", "3b": "13", "5b":"15", "8b":"18", "7a":"7", "7b":"9"}
	simplified = 2
	
	def format(self, line):
		for i,j in self.toneValues.items():
			line = line.replace("[%s]" % i, "[%s]" % j)
		return line
