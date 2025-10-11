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
