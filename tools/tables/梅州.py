#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "hak_yt_mz"
	_file = "梅县话*.tsv"
	note = "來源：<u>林</u>整理自《梅縣話同音字彙》"
	tones = "44 1 1a 陰平 ꜀,22 2 1b 陽平 ꜁,31 3 2 上 ꜂,,52 5 3 去 ꜄,,1 7 4a 陰入 ꜆,5 8 4b 陽入 ꜇"
	toneValues = {"44":1, "22":2,"31":3,"52":5,"1":7,"5":8}
	simplified = 2

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
