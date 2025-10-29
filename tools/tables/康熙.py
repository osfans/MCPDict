#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	全稱 = "康熙字典"
	文件名 = "kangxizidian-v3f.txt"
	_sep = "\t\t"
	網站 = "康熙字典網上版"
	網址 = "https://kangxizidian.com/kxhans/%s"
	說明 = "來源：<a href=https://github.com/7468696e6b/kangxiDictText/>康熙字典 Kangxi Dictionary TXT</a>"
	字書 = True
	字組 = set()
	
	def 析(自, 列):
		字 = 列[0]
		js = "\t".join(列[1:])
		if len(字) > 1:
			js = js.replace("\t", f"\t({字})", 1)
			字 = 字[0]
		return 字, js
