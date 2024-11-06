#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	full = "康熙字典"
	short = "康熙"
	_file = "kangxizidian-v3f.txt"
	_sep = "\t\t"
	site = "康熙字典網上版"
	url = "https://kangxizidian.com/kxhans/%s"
	note = "來源：<a href=https://github.com/7468696e6b/kangxiDictText/>康熙字典 Kangxi Dictionary TXT</a>"
	ybTrimSpace = False
	dictionary = True
	
	def parse(self, fs):
		hz, js = fs
		js = js.replace("", "\t").strip()[6:]
		js = re.sub(r"頁(\d+)第(\d+)\t", lambda x: "%04d.%d"%(int(x[1]),int(x[2])), js)
		return hz, js
