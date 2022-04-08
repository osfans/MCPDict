#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "hak_yt_mz"
	_file = "梅县话*.tsv"
	toneValues = {"44":1, "22":2,"31":3,"52":5,"1":7,"5":8}

	def format(self, line):
		line = line.replace('"', '')
		if "\t" in line:
			fs = line.split("\t")
			line = fs[0]+"\t"
			for i in fs[1:]:
				if i.startswith("["):
					results = re.findall("^\[(\d+)\](.+)$", i)
					if results:
						sd,hzs = results[0]
						sd = "[%s]"%self.toneValues[sd]
						i = sd + hzs
				line += i
		return line
