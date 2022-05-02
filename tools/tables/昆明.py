#!/usr/bin/env python3

from tables._縣志 import 表 as _表
import re

class 表(_表):

	def format(self, line):
		if "调值" in line: return ""
		line = re.sub("^.*?\t", "", line)
		line = line.replace('""	"', '"#')\
				.replace("①","[1]").replace("②","[2]").replace("③","[3]").replace("④","[4]")\
				.replace(")","）").replace("（","｛").replace("）","｝")
		return line

