#!/usr/bin/env python3

from openpyxl import load_workbook
from collections import defaultdict
import os.path

from tables._表 import 表 as _表
import re

class 表(_表):

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
