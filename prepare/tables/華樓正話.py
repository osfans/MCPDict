#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表

class 字表(表):
	key = "cmn_fyd_hlzh"
	_color = "#FF0000,#FFAD00"
	_file = "华楼正话同音字表20211129.tsv"
	note = "版本：2021-11-29<br>來源：<u>清竮塵</u>整理自陳雲龍《廣東電白舊時正話》"
	tones = "33 1 1a 陰平 ꜀,213 2 1b 陽平 ꜁,31 3 2 上 ꜂,,55 5 3 去 ꜄,,5 7a 4a 短入 ꜆,214 7b 4b 長入 ꜀"

	def update(self):
		d = defaultdict(list)
		for line in open(self.spath):
			line = line.strip('\n')
			fs = [i.strip('" ') for i in line.split('\t')]
			if not fs: continue
			if fs[0].startswith("#"):
				ym = fs[0][1:]
				continue
			if len(fs) < 2: continue
			sm = fs[0].replace("Ø", "")
			for sd,hzs in re.findall("\[(\d+[ab]?)\]([^\[\]]+)", fs[1]):
				if sd == "7a": sd = "7"
				elif sd == "7b": sd = "8"
				py = sm + ym + sd
				hzm = re.findall("(.)\d?(\{.*?\})?", hzs)
				for hz, m in hzm:
					js = m.strip("{}")
					p = "%s\t%s"%(py, js)
					if p not in d[hz]:
						d[hz].append(p)
		self.write(d)

