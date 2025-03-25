#!/usr/bin/env python3

import re
from tables._數據庫 import 表 as _表

class 表(_表):
	鍵 = "mn"
	補丁 = {"檔": "tong2,tong3"}

	def 統(自, py):
		py = re.sub(r"\|(.*?)\|", "\\1\t白", py)
		py = re.sub(r"\*(.*?)\*", "\\1\t文", py)
		py = re.sub(r"\((.*?)\)", "\\1\t俗", py)
		py = re.sub(r"\[(.*?)\]", "\\1\t替", py)
		return py
	
	def 修訂(自, d):
		for 行 in open(自.全路徑("豆腐台語詞庫.csv"),encoding="U8"):
			列 = 行.strip().split(',')
			字 = 列[0]
			if len(字) == 1:
				for py in 列[1:]:
					if py not in d[字]:
						d[字].append(py)
		_表.修訂(自, d)
