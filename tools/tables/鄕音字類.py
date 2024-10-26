#!/usr/bin/env python3

from tables._表 import 表 as _表
import os
from openpyxl import load_workbook

class 表(_表):
	note = ""

	def read(self):
		super().read()
		self.note = self.get_note()

	def get_note(self):
		sname = self.get_fullname(self._file)
		if not os.path.exists(sname) or not sname.endswith(".xlsx"): return
		wb = load_workbook(sname, data_only=True)
		sheet = wb.worksheets[1]
		lines = list()
		for row in sheet.rows:
			fs = [j.value if j.value else "" for j in row[:50]]
			if any(fs):
				line = "\t".join(fs)
				if line:
					lines.append(line.lstrip("#"))
		return "\n".join(lines)

	def parse(self, fs):
		if len(fs) < 4: return
		hz, jt, py, js = fs[:4]
		py = py.replace("øʏ","𐞢ʏ")
		if py.endswith("="):
			js = "(書)%s" % js
		elif py.endswith("-"):
			py = py[:-1] + "="
		js = js.strip().replace("|", "｜")
		if not hz: hz = jt
		return hz, py, js

