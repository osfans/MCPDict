#!/usr/bin/env python3

from tables._數據庫 import 表 as _表

class 表(_表):
	鍵 = "vn"

	@property
	def 聲韻數(自):
		return len(set(map(lambda x:x.rstrip("zrsfxj"), 自.音典.keys())))
