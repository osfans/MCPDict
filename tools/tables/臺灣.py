#!/usr/bin/env python3

import re
from tables._數據庫 import 字表 as 表

class 字表(表):
	key = "nan_zq_tw"
	dbkey = "mn"
	site = "臺灣閩南語常用詞辭典"
	url = "http://twblg.dict.edu.tw/holodict_new/result.jsp?querytarget=1&radiobutton=0&limit=20&sample=%s"
	patches = {"檔": "tong2,tong3"}

	def format(self, py):
		py = re.sub("\|(.*?)\|", "\\1-", py)
		py = re.sub("\*(.*?)\*", "\\1=", py)
		py = re.sub("\((.*?)\)", "\\1*", py)
		py = re.sub("\[(.*?)\]", "\\1≈", py)
		return py
	
	def patch(self, d):
		for line in open(self.get_fullname("豆腐台語詞庫.csv"),encoding="U8"):
			fs = line.strip().split(',')
			hz = fs[0]
			if len(hz) == 1:
				for py in fs[1:]:
					if py not in d[hz]:
						d[hz].append(py)
		表.patch(self, d)
