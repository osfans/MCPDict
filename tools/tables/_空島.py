#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	simplified = 2
	
	def format(self, line):
		if "调值" in line: return ""
		line = line.replace("*", "□").replace("（", "(").replace("）", ")").replace("｛", "[").replace("｝", "]")
		line = re.sub("\[(.*?)\((.*?)\)(.*?)\]","{\\1（\\2）\\3}",line)
		line = line.replace("(", "{").replace(")","}")
		line = re.sub('^""\t"', '"#', line)
		for i, j in self.toneValues.items():
			line = line.replace(i, "［%d］"%j)
		return line
