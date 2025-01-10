#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	文件名 = "Cangjie5.tsv"
	全稱 = "倉頡五代"
	說明 = "來源：https://github.com/Jackchows/Cangjie5"

	def 析(自, 列):
		if len(列) < 2: return
		return 列[0], 列[1]
