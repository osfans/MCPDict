#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	全稱 = "古音匯纂"
	文件名 = "古音匯纂.tsv"
	說明 = ""
	字書 = True

	def 析(自, 列):
		return 列[0], 列[1], re.sub("\\|.*?\t", "\t", "\t".join(列[2:]).replace("▲", "\t▲"))
