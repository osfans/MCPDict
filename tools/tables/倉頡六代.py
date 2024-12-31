#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	_file = "cangjie6.dict.yaml"
	簡稱 = "倉六"
	說明 = "來源：https://github.com/LEOYoon-Tsaw/Cangjie6"
	patches = {"□": "bu"}

	def 統(自, 行):
		if 行.startswith("#") and "\t" in 行: return 行[1:]
		return 行

	def 析(自, 列):
		if len(列) < 2: return
		return 列[0], 列[1]
