#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表 as _表

class 表(_表):

	def update(self):
		d = defaultdict(list)
		for line in open(self.spath,encoding="U8"):
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

