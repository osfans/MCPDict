#!/usr/bin/env python3

import sqlite3, re
from collections import defaultdict
from tables._數據庫 import 表 as _表

class 表(_表):
	def 更新(自):
		d = defaultdict(list)
		conn = sqlite3.connect(自.spath)
		conn.row_factory = sqlite3.Row
		c = conn.cursor()
		for r in c.execute('SELECT * FROM mcpdict'):
			字 = chr(int(r["unicode"],16))
			for 鍵 in ["jp_tou", "jp_kwan", "jp_other"]:
				pys = r[鍵]
				if not pys: continue
				pys = re.sub(r"\[\d\]", ",",pys).strip(",")
				for py in pys.split(","):
					py = py.strip()
					if not py: continue
					yb = 自.統(py)
					if 鍵 == "jp_tou":
						yb += "\t唐"
					elif 鍵 == "jp_kwan":
						yb += "\t慣"
					d[字].append(yb)
		conn.close()
		自.寫(d)
