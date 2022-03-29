#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_zho_xb_ls"
	tones = "54 1a 1a 陰平 ꜀,55 2 1b 陽平 ꜁,324 3 2 上 ꜂,,31 5 3 去 ꜄"
	_file = "罗山.tsv"
	simplified = 2

	def format(self, line):
		line = line.replace(": [", "	[").replace("：[", "	[").replace("ø","Ø")
		return line
