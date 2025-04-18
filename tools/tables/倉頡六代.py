#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	文件名 = "cangjie6.dict.yaml"
	全稱 = "倉頡六代"
	說明 = "來源：https://github.com/LEOYoon-Tsaw/Cangjie6"
	補丁 = {"□": "bu"}

	def 統(自, 行):
		if 行.startswith("#") and "\t" in 行: return 行[1:]
		return 行

	def 析(自, 列):
		if len(列) < 2: return
		return 列[0], 列[1]
