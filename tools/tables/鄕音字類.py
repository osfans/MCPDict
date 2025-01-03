#!/usr/bin/env python3

from tables._表 import 表 as _表
import os
from openpyxl import load_workbook

class 表(_表):
	def 讀(自):
		super().讀()
		自.說明 = 自.get_note()

	def get_note(自):
		sname = 自.全路徑(自.文件名)
		if not os.path.exists(sname) or not sname.endswith(".xlsx"): return
		wb = load_workbook(sname, data_only=True)
		sheet = wb.worksheets[1]
		lines = list()
		for row in sheet.rows:
			列 = [j.value if j.value else "" for j in row[:50]]
			if any(列):
				行 = "\t".join(列)
				if 行:
					lines.append(行.lstrip("#"))
		return "\n".join(lines)

	def 析(自, 列):
		if len(列) < 4: return
		字, jt, py, js = 列[:4]
		py = py.replace("øʏ","𐞢ʏ")
		if py.endswith("="):
			js = "(書)%s" % js
		elif py.endswith("-"):
			py = py[:-1] + "="
		js = js.strip().replace("|", "｜").replace("{", "[").replace("}", "]")
		if not 字: 字 = jt
		return 字, py, js

