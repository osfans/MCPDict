#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "czh_txcsh"
	tones = "53 1 1a 陰平 ꜀,55 2 1b 陽平 ꜁,21 3 2 上 ꜂,,22 5 3a 陰去 ꜄,324 6 3b 陽去 ꜅,,,,,53 1b 1a 連讀高降調 ꜀,55 2b 1c 連讀高平調 ꜁,21 3b 2b 連讀低降調 ꜂,,22 5b 3c 連讀低平調 ꜄,324 6b 3d 連讀曲折調 ꜅"
	_file = "屯溪船上话同音字表*.tsv"
	toneValues = {"1b": "11", "2b": "12", "3b": "13", "5b":"15", "6b":"16"}
	simplified = 2
	
	def format(self, line):
		for i,j in self.toneValues.items():
			line = line.replace("[%s]" % i, "[%s]" % j)
		return line
