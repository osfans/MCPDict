#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	全稱 = "說文解字"
	網站 = "說文解字線上搜索"
	網址 = "http://www.shuowen.org/?kaishu=%s"
	說明 = "來源：<a href=https://github.com/shuowenjiezi/shuowen/>說文解字網站數據</a>"
	字書 = True
	
	def 析(自, 列):
		fq = 列[1].split(" ")[0]
		return 列[0], fq, "\t".join(列[2:])
