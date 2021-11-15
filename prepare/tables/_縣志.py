#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表

class 字表(表):
	def update(self):
		d = defaultdict(list)
		for line in open(self.spath):
			line = line.strip().replace('"','').replace("[","［").replace("]", "］").replace("?","？")
			if not line: continue
			if line.startswith("#"):
				ym = line[1:].split()[0]
				continue
			fs = line.split("\t")[:2]
			if len(fs) != 2: continue
			sm = fs[0]
			for sd,hzs in re.findall("［(\d+)］([^［］]+)", fs[1]):
				py = sm + ym +sd
				py = py.replace("0", "").strip("Ø∅")
				hzs = re.findall("(.)\d?([+\-/=\*？]?)\d?(\{.*?\})?", hzs)
				for hz, c, m in hzs:
					if hz == " ": continue
					m = m.strip("{}")
					p = ""
					if c and c in '-+/=*？':
						if c == '-':
							p = "白"
						elif c == '=':
							p = "文"
						elif c == '*':
							p = "俗"
						elif c == '/':
							p = "書"
						elif c == '？':
							p = "待考"
					if m and p: p = "(%s)"%p
					p = py + "\t" + p + m
					if p not in d[hz]:
						if c == '-':
							d[hz].insert(0, p)
						else:
							d[hz].append(p)
		self.write(d)
