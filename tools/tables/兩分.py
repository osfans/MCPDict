#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	文件名 = "liangfen.dict.yaml"
	說明 = "來源：<a href=http://yedict.com/zslf.htm>兩分查字</a><br>說明：可以輸入“雲龍”或“yunlong”查到“𱁬”"
	
	def 析(自, 列):
		if len(列) < 2: return
		return 列[0], 列[1]

	def 修訂(自, d):
		for 行 in open(自.全路徑("lfzy.tsv"),encoding="U8"):
			列 = 行.strip().split("\t")
			字, lf = 列[:2]
			if "(" in lf:
				if " " in lf:
					a,b=lf.split(" ")
					for i in a.strip(")").split("("):
						for j in b.strip(")").split("("):
							d[字].append(i+j)
				else:
					for j in b.strip(")").split("("):
						d[字].append(j)
			else:
				lf = lf.replace(" ", "")
				d[字].append(lf)
