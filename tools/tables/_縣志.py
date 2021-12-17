#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表

class 字表(表):
	disorder = True
	def update(self):
		d = defaultdict(list)
		ym = ""
		for line in open(self.spath):
			line = self.format(line)
			line = line.strip().replace("Ǿ", "ˀ").replace('"','').replace("[","［").replace("]", "］").replace("?","？")
			if not line: continue
			if line.startswith("#"):
				ym = line[1:]
				if not ym: continue
				ym = ym.split()[0]
				continue
			fs = line.split("\t")[:2]
			if len(fs) != 2: continue
			sm = fs[0]
			for sd,hzs in re.findall("［(\d+)］([^［］]+)", fs[1]):
				if sd == "0": sd = ""
				py = sm + ym +sd
				py = py.lstrip("0Ø∅")
				hzs = re.findall("(.)\d?([+\-/=~≈\\\*？$&]?)\d?(\{.*?\})?", hzs)
				for hz, c, js in hzs:
					if hz == " ": continue
					p = ""
					if c:
						if c in "+-*/=~≈\\":
							pass
						else:
							if c == '？':
								p = ""
								c = "?"
							elif c == '$':
								p = "(单字调)"
								c = ""
							elif c == '&':
								p = "(连读前字调)"
								c = ""
					js = js.strip("{}")
					p = py + c + "\t" + p + js
					if p not in d[hz]:
						d[hz].append(p)
		self.write(d)
