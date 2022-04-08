#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "hak_nl_qndjs"
	_file = "江西全南大吉山镇客家方言同音字汇.tsv"
	toneValues = {'阴平24':1,'阳平314':2,'上声52':3,'阴去44':5,'阳去32':6,'阴入54':7,'阳入5':8}

	def format(self,line):
		for i in self.toneValues.keys():
			line = line.replace(f"[{i}]",f"[{self.toneValues[i]}]")
		line = line.replace("<","{").replace(">","}")
		return line
