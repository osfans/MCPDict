#!/usr/bin/env python3

from tables._縣志 import 表 as _表
import re

class 表(_表):
	def format(self, line):
		line = line.replace('"', '')
		if "\t" in line:
			fs = line.split("\t")
			line = fs[0]+"\t"
			for i in fs[1:]:
				if i.startswith("["):
					results = re.findall(r"^\[(\d+)\](.+)$", i)
					if results:
						sd,hzs = results[0]
						sd = "[%s]"%self.toneMaps[sd]
						i = sd + hzs
				line += i
		return line
