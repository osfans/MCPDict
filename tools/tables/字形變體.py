#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables import hex2chr

class 表(_表):
	_files = ["uvs0.txt", "uvs2.txt", "uvs3.txt"]

	def 析(自, 列):
		字, vs = 列
		字 = hex2chr(字)
		vs = 字 + hex2chr(vs.split(";", 1)[0])
		return 字, vs
