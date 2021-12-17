#!/usr/bin/env python3

import sqlite3
from collections import defaultdict
from tables._表 import 表

class 字表(表):
	_file = "mcpdict.db"
	isYb = False

	def update(self):
		d = defaultdict(list)
		conn = sqlite3.connect(self.spath)
		conn.row_factory = sqlite3.Row
		c = conn.cursor()
		for r in c.execute('SELECT * FROM mcpdict'):
			hz = chr(int(r["unicode"],16))
			pys = r[self.dbkey]
			if not pys: continue
			for py in pys.split(","):
				py = py.strip()
				if not py: continue
				yb = self.format(py)
				d[hz].append(yb)
		conn.close()
		self.write(d)
