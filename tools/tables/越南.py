#!/usr/bin/env python3

from tables._數據庫 import 表 as _表

class 表(_表):
	鍵 = "vn"

	def 分音(自, 音):
		if 音[-1] in "zrsfxj":
			調 = 音[-1]
			聲韻 = 音[:-1]
		else:
			調 = "1"
			聲韻 = 音
		return 聲韻,調