#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_xn_yn_dz_km"
	_file = "云南省昆明市方言同音字表*.tsv"

	def format(self, line):
		if "调值" in line: return ""
		line = line.replace('""	"', '"#')\
				.replace("①","[1]").replace("②","[2]").replace("③","[3]").replace("④","[4]")\
				.replace(")","）").replace("（","｛").replace("）","｝")
		return line

