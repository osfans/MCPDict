#!/usr/bin/env python3

import re
from tables._數據庫 import 表 as _表

class 表(_表):
	dbkey = "mn"
	site = "臺灣閩南語常用詞辭典"
	url = "http://twblg.dict.edu.tw/holodict_new/result.jsp?querytarget=1&radiobutton=0&limit=20&sample=%s"
	patches = {"檔": "tong2,tong3"}

	def format(self, py):
		py = re.sub("\|(.*?)\|", "\\1\t白", py)
		py = re.sub("\*(.*?)\*", "\\1\t文", py)
		py = re.sub("\((.*?)\)", "\\1\t俗", py)
		py = re.sub("\[(.*?)\]", "\\1\t替", py)
		return py
	
	def patch(self, d):
		for line in open(self.get_fullname("豆腐台語詞庫.csv"),encoding="U8"):
			fs = line.strip().split(',')
			hz = fs[0]
			if len(hz) == 1:
				for py in fs[1:]:
					if py not in d[hz]:
						d[hz].append(py)
		_表.patch(self, d)
