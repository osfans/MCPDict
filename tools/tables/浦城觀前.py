#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "mnp_jo_pcgq"
	_file = "浦城观前.tsv"

	def format(self, line):
		line = line.replace("", "Ø").replace("", "")
		line = re.sub("^(.*?)［", "\\1	［", line)
		return line
