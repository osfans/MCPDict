#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表

class 字表(表):
	def update(self):
		d = defaultdict(list)
		for line in open(self.spath,encoding="U8"):
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
				for c, hz, js in re.findall("([？#\-\+])?(.)(（[^（）]*?（.*?）.*?）|（.*?）)?", hzs):
					if hz == " ": continue
					if js: js = js[1:-1]
					p = ""
					if c == '-':
						p = "舊"
						c = ""
					elif c == '+':
						c = "/"
					elif c == '？':
						c = "?"
					elif c == '#':
						c = "*"
					if p: p = f"({p})"
					p += js.strip()
					if p.startswith("(") and p.endswith(")"): p = p[1:-1]
					p = py + c + "\t" + p
					if p not in d[hz]:
						d[hz].append(p)
		self.write(d)
