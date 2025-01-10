#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	文件名 = "cj3.txt"
	全稱 = "倉頡三代"
	說明 = "來源：https://github.com/Arthurmcarthur/Cangjie3-Plus"

	def 析(自, 列):
		return 列[-1], 列[0]
