#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表

class 字表(表):
	key = "wuu_th_pl_jt"
	note = "版本：2021-12-17<br>來源：金壇縣志，轉錄者<u>正心修身</u>"
	tones = "434 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,44 3 2a 陰上 ꜂,21 4 2b 陽上 ꜃,55 5 3a 陰去 ꜄,213 6 3b 陽去 ꜅,4 7 4a 陰入 ꜆,2 8a 4b 陽入 ꜇"
	_file = "金壇話.tsv"
	simplified = 2

	def update(self):
		d = defaultdict(list)
		for line in open(self.spath):
			line = line.strip().replace('"','').replace(' ','').rstrip()
			if '\t' not in line: continue
			fs = line.split("\t")
			sy = fs[0]
			for sd,hzs in re.findall("([①-⑧])([^①-⑧]+)", fs[1]):
				sd = ord(sd) - ord('①') + 1
				py = sy + str(sd)
				hzs = re.findall("(.)(\*)?(［\\d］)?(（.*?）)?", hzs)
				for hz, s, c, js in hzs:
					js = js.strip("（）")
					p = "%s%s\t%s" % (c, py, js)
					if p not in d[hz]:
						d[hz].append(p)
		for hz in d.keys():
			d[hz] = [i[3:] if i.startswith("［") else i for i in sorted(d[hz])]
		self.write(d)

