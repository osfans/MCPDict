#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables import hex2chr

class 表(_表):
	文件名 = "IVD_Sequences.txt"

	def 析(自, 列):
		字, 變體 = 列[:2]
		字 = hex2chr(字)
		變體 = 字 + hex2chr(變體.rstrip(";"))
		return 字, 變體

	def 修訂(自, d):
		for 行 in open(自.全路徑("StandardizedVariants.txt"),encoding="U8"):
			行 = 行.strip()
			if 行.startswith("#"): continue
			if "CJK" not in 行: continue
			fields = 行.strip().split("; ")
			a, b = fields[0].split(" ")
			字 = hex2chr(a)
			變體 = hex2chr(a) + hex2chr(b)
			d[字].append(變體)
		_表.修訂(自, d)