#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	全稱 = "集韻"
	說明 = "來源：<a href=https://github.com/guavajuice/qieyun/blob/main/public/data/zhup_hyun.csv/>切韵查詢</a>"
	字書 = True
	文件名 = "集韻.csv"
	
	def 析(自, 列):
		字 = 列[1][0]
		ids = 列[1][1:]
		if not ids.startswith("("): ids = ""
		return 字, f"{列[11]}{列[4]}。{列[12]}", f"{ids}{列[13]}"
