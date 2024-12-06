#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables import hex2chr, hzorders

class 表(_表):
	note = "方言調查字表"
	_file = "方言調查字表"

	def parse(self, fs):
		return fs[0], "FD"

	def patch(self, d):
		for line in open(self.get_fullname("Unihan_OtherMappings.txt"),encoding="U8"):
			if not line.startswith("U"): continue
			hz, typ, val = line.strip().split("\t", 2)
			hz = hex2chr(hz)
			if typ in ("kBigFive",):
				d[hz].append(typ)
		for line in open(self.get_fullname("Unihan_DictionaryLikeData.txt"),encoding="U8"):
			if not line.startswith("U"): continue
			hz, typ, val = line.strip().split("\t", 2)
			if typ == "kHKGlyph":
				hz = hex2chr(hz)
				d[hz].append(typ)
		for line in open(self.get_fullname("Unihan_IRGSources.txt"),encoding="U8"):
			if not line.startswith("U"): continue
			hz, typ, val = line.strip().split("\t", 2)
			if typ == "kIRG_TSource":
				hz = hex2chr(hz)
				hzorders[hz] = list(map(lambda x:int(x, 16), val.lstrip("T").replace("U", "100").split("-")))
				if val.startswith("T1-"):
					d[hz].append("T1")
		for line in open(self.get_fullname("古籍印刷通用字規範字形表.txt"),encoding="U8"):
			if line.startswith("#"): continue
			index, uni, hzs = line.strip().split(" ")
			d[hzs[0]].append("GJ")
