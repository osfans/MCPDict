#!/usr/bin/env python3

from tables._縣志 import 表 as _表
import re

class 表(_表):
	
	def format(self, line):
		line = line.replace("*", "□").replace("(","（").replace(")","）").replace("（","{").replace("）","}").replace("、","/")
		line = line.replace("윽", "")
		if line.startswith("#"):
			line = re.sub('^(#[^ ]*) .*?	', '\\1', line)
		else:
			line = line.split("\t", 1)[1]
			line = re.sub('^([^ ]+) .*?	', '\\1', line)
			line = re.sub('^([^ ]+)	', '\\1', line)
			line = re.sub('^(.*?)/.*?	', '\\1', line)
		return line
