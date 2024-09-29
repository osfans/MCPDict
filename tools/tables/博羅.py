#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表 as _表

class 表(_表):

	def update(self):
		d = defaultdict(list)
		for line in open(self.spath,encoding="U8"):
			line = line.strip().replace('"','').replace("(","（").replace(")", "）").replace("[","［").replace("]", "］").replace("?","？")
			if not line: continue
			if line.startswith("#"):
				ym = line[1:].split()[0]
				continue
			if "［" not in line: continue
			fs = line.split("\t", 1)
			if len(fs) != 2: continue
			sm = fs[0].strip("ø ")
			for sd,hzs in re.findall(r"［(\d+)］([^［］]+)", fs[1].replace("\t","")):
				yb = self.dz2dl(sm + ym, sd)
				hzs = re.findall(r"(.)(（[^）]*?（.*?）.*?）|（.*?）)?", hzs)
				for hz, js in hzs:
					if hz == " ": continue
					js = js[1:-1]
					p = "%s\t%s" % (yb, js)
					if p not in d[hz]:
						d[hz].append(p)
		self.write(d)

