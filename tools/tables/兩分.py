#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "lf"
	_file = "liangfen.dict.yaml"
	_lang = "兩分"
	note = "來源：<a href=http://yedict.com/zslf.htm>兩分查字</a><br>說明：可以輸入“雲龍”或“yunlong”查到“𱁬”"
	
	def parse(self, fs):
		if len(fs) < 2: return
		return fs[0], fs[1]

	def patch(self, d):
		for line in open(self.get_fullname("lfzy.tsv"),encoding="U8"):
			fs = line.strip().split("\t")
			hz, lf = fs[:2]
			if "(" in lf:
				if " " in lf:
					a,b=lf.split(" ")
					for i in a.strip(")").split("("):
						for j in b.strip(")").split("("):
							d[hz].append(i+j)
				else:
					for j in b.strip(")").split("("):
						d[hz].append(j)
			else:
				lf = lf.replace(" ", "")
				d[hz].append(lf)
