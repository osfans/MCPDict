#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables import hex2chr, hzorders
import re

class 表(_表):
	文件名 = "方言調查字表.tsv"

	def 析(自, 列):
		return re.sub("\\d", "", 列[1].lstrip("·*⌈⌊[")[0]), "FD"

	def 修訂(自, d):
		for 行 in open(自.全路徑("Unihan_OtherMappings.txt"),encoding="U8"):
			if not 行.startswith("U"): continue
			字, 類, 值 = 行.strip().split("\t", 2)
			字 = hex2chr(字)
			if 類 in ("kBigFive",):
				d[字].append(類)
		for 行 in open(自.全路徑("Unihan_DictionaryLikeData.txt"),encoding="U8"):
			if not 行.startswith("U"): continue
			字, 類, 值 = 行.strip().split("\t", 2)
			if 類 == "kHKGlyph":
				字 = hex2chr(字)
				d[字].append(類)
		for 行 in open(自.全路徑("Unihan_IRGSources.txt"),encoding="U8"):
			if not 行.startswith("U"): continue
			字, 類, 值 = 行.strip().split("\t", 2)
			if 類 == "kIRG_TSource":
				字 = hex2chr(字)
				hzorders[字] = list(map(lambda x:int(x, 16), 值.lstrip("T").replace("U", "100").split("-")))
				if 值.startswith("T1-"):
					d[字].append("T1")
		for 行 in open(自.全路徑("古籍印刷通用字規範字形表.txt"),encoding="U8"):
			if 行.startswith("#"): continue
			index, uni, 字組 = 行.strip().split(" ")
			d[字組[0]].append("GJ")
		for 行 in open(自.全路徑("廣韻.tsv"),encoding="U8"):
			字 = 行.strip().split("\t")[1]
			if len(字) == 1:
				d[字].append("GY")