#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表

class 字表(表):
	key = "hak_yz_bl,yue_"
	_lang = "博羅本地話"
	note = "版本：2021-11-20<br>來源：<u>Kiattan</u>整理自《博羅方言同音字彙》"
	tones = "44 1 1a 陰平 ꜀,11 2 1b 陽平 ꜁,35 3 2 上 ꜂,,13 5 3a 陰去 ꜄,42 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,2 8a 4b 陽入 ꜇,55 9 5 高平變調 "
	_file = "博罗本地话同音字表.tsv"
	toneValues = {"44":1, "11":2,"35":3,"13":5,"42":6,"5":7,"2":8,"55":9}

	def update(self):
		d = defaultdict(list)
		for line in open(self.spath):
			line = line.strip().replace('"','').replace("(","（").replace(")", "）").replace("[","［").replace("]", "］").replace("?","？")
			if not line: continue
			if line.startswith("#"):
				ym = line[1:].split()[0]
				continue
			if "［" not in line: continue
			fs = line.split("\t", 1)
			if len(fs) != 2: continue
			sm = fs[0].strip("ø ")
			for sd,hzs in re.findall("［(\d+)］([^［］]+)", fs[1].replace("\t","")):
				yb = sm + ym + str(self.toneValues[sd])
				hzs = re.findall("(.)(（[^）]*?（.*?）.*?）|（.*?）)?", hzs)
				for hz, js in hzs:
					if hz == " ": continue
					js = js[1:-1]
					p = "%s\t%s" % (yb, js)
					if p not in d[hz]:
						d[hz].append(p)
		self.write(d)

