#!/usr/bin/env python3

import sqlite3, re
from collections import defaultdict
from tables._表 import 表 as _表

class 表(_表):
	文件名 = "mcpdict.db"
	爲音 = False

	def 統(自, 行):
		行 = 行.replace("|", "`").replace("*", "**")
		return 行

	def 更新(自):
		d = defaultdict(list)
		conn = sqlite3.connect(自.spath)
		conn.row_factory = sqlite3.Row
		c = conn.cursor()
		for r in c.execute('SELECT * FROM mcpdict'):
			字 = chr(int(r["unicode"],16))
			pys = r[自.鍵]
			if not pys: continue
			pys = re.sub(r"\[\d\]", ",",pys).strip(",")
			for py in pys.split(","):
				py = py.strip()
				if not py: continue
				yb = 自.統(py)
				d[字].append(yb)
		conn.close()
		自.寫(d)
