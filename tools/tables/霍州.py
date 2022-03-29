#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_zho_fh_py_hz"
	_file = "霍州话同音字表*.tsv"
	tones = "11 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,332 3 2 上 ꜂,,55 5 3a 陰去 ꜄,51 6 3b 陽去 ꜅,5 0a 0a 輕聲A ,1 0b 0b 輕聲B "
	simplified = 2
	
	def format(self, line):
		line = line.replace("0a", "7").replace("0b", "8")
		return line
