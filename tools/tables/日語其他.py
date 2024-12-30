#!/usr/bin/env python3

import sqlite3, re
from collections import defaultdict
from tables._數據庫 import 表 as _表

class 表(_表):
	def update(self):
		d = defaultdict(list)
		conn = sqlite3.connect(self.spath)
		conn.row_factory = sqlite3.Row
		c = conn.cursor()
		for r in c.execute('SELECT * FROM mcpdict'):
			hz = chr(int(r["unicode"],16))
			for dbkey in ["jp_tou", "jp_kwan", "jp_other"]:
				pys = r[dbkey]
				if not pys: continue
				pys = re.sub(r"\[\d\]", ",",pys).strip(",")
				for py in pys.split(","):
					py = py.strip()
					if not py: continue
					yb = self.format(py)
					if dbkey == "jp_tou":
						yb += "\t唐"
					elif dbkey == "jp_kwan":
						yb += "\t慣"
					d[hz].append(yb)
		conn.close()
		self.write(d)
