#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表

class 字表(表):
	def update(self):
		d = defaultdict(list)
		for line in open(self.spath):
			line = line.strip().replace(",","，").replace(";","；").replace(":","：").replace("？（", "□（")
			if line.startswith("#"):
				ym = line[1:]
				continue
			fs = re.findall("^(.*?)[ø\t ]*([①-⑧])", line)
			if not fs: continue
			sm = fs[0][0]
			for sd,hzs in re.findall("([①-⑧])([^①-⑧]+)", line):
				sd = ord(sd) - ord('①') + 1
				py = sm + ym + str(sd)
				for c, hz, m in re.findall("([？#\-\+])?(.)(（[^（）]*?（.*?）.*?）|（.*?）)?", hzs):
					if hz == " ": continue
					p = ""
					if c == '+':
						p = "書"
					if c == '-':
						p = "舊"
					elif c == '#':
						p = "俗"
					elif c == '？':
						p = "存疑"
					if m:
						p += " " + m[1:-1]
					p = p.strip()
					p = py + "\t" + p
					if p not in d[hz]:
						d[hz].append(p)
		self.write(d)
