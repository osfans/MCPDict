#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "nan_cs_chdx"
	simplified = 2
	tones = "33 1 1a 陰平 ꜀,55 2 1b 陽平 ꜁,53 3 2a 陰上 ꜂,35 4 2b 陽上 ꜃,213 5 3a 陰去 ꜄,11 6 3b 陽去 ꜅,2 7 4a 陰入 ꜆,4 8 4b 陽入 ꜇"
	toneValues = {'33':1,'55':2,'53':3,'35':4,'213':5,'11':6,'2':7,'4':8}

	def format(self,line):
		for i in sorted(self.toneValues.keys(),key=len):
			line = line.replace(f"[{i}]",f"[{self.toneValues[i]}]")
		line = line.replace("（","{").replace("）","}")
		return line
