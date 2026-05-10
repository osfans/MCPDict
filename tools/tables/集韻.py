#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	全稱 = "集韻"
	說明 = "來源：<a href=https://github.com/guavajuice/qieyun/blob/main/public/data/zhup_hyun.csv/>切韵查詢</a>"
	字書 = True
	文件名 = "集韻.csv"
	中文序號 = "一二三四五六七八九十"
	
	def 析(自, 列):
		字 = 列[2][0]
		卷 = 自.中文序號[int(列[0]) - 1]
		音 = f"{列[12]}{卷}{列[5]}。{列[13]}".replace("(湩)", "腫").replace("(櫬)", "稕")
		註 = 列[14]
		ids = 列[2][1:]
		if ids.startswith("("): 註 = ids + 註
		return 字, 音, 註
