#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_zho_xb_ls"
	_file = "罗山.tsv"

	def format(self, line):
		line = line.replace(": [", "	[").replace("：[", "	[").replace("ø","Ø")
		return line
