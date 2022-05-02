#!/usr/bin/env python3

from tables._縣志 import 表 as _表

class 表(_表):
	toneValues = {'33':1,'55':2,'53':3,'35':4,'213':5,'11':6,'2':7,'4':8}

	def format(self,line):
		for i in sorted(self.toneValues.keys(),key=len):
			line = line.replace(f"[{i}]",f"[{self.toneValues[i]}]")
		line = line.replace("（","{").replace("）","}")
		return line
