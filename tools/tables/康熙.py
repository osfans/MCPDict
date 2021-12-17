#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "kx"
	_lang = "康熙字典"
	_file = "kangxizidian-v3f.txt"
	_sep = "\t\t"
	site = "康熙字典網上版"
	url = "https://kangxizidian.com/kxhans/%s"
	note = "來源：<a href=https://github.com/7468696e6b/kangxiDictText/>康熙字典 Kangxi Dictionary TXT</a>"
	hasHead = False
	ybTrimSpace = False
	
	def parse(self, fs):
		hz, js = fs
		js = js.replace("", "\t").strip()[6:]
		js = re.sub("頁(\d+)第(\d+)\t", lambda x: "%04d.%d"%(int(x[1]),int(x[2])), js)
		return hz, js
