#!/usr/bin/env python3

from openpyxl import load_workbook
from collections import defaultdict
import os.path

from tables._表 import 表
import re

class 字表(表):
	key = "ltc_yt"
	note = "來源：QQ共享文檔<a href=https://docs.qq.com/sheet/DYk9aeldWYXpLZENj>韻圖音系同音字表</a>"
	tones = " 1 1 平 ꜀, 3 2 上 ꜂, 5 3 去 ꜄, 7 4 入 ꜆"
	_file = "Dzih.txt"
	hasHead = False

	def get_dict(self):
		yt = dict()
		for sheet in load_workbook(self.get_fullname("韵图音系同音字表.xlsx")):
			for row in sheet.rows:
				y = row[0].value
				for cell in row[1:]:
					if v := cell.value:
						if type(v) is float:
							yt[int(v)]=y
						elif type(v) is str and "#" in v:
							for i in v.split("#"):
								yt[int(i)]=y
		for i in sorted(yt.keys()):
			y = yt[i]
			t = y[-1]
			if not t.isdigit(): y = y + "4"
			yt[i] = y
		return yt
	
	def __init__(self):
		self.yt = self.get_dict()

	def parse(self, fs):
		hz = fs[0]
		yb = self.yt[int(fs[1])]
		return hz, yb
