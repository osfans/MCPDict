#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	文件名 = "wb.csv"
	說明 = "來源：<a href=https://github.com/CNMan/UnicodeCJK-WuBi>五筆字型Unicode CJK超大字符集編碼數據庫</a>、<a href=https://github.com/yanhuacuo/98wubi-unicode>98五筆超大字符集碼表</a><br>說明：12345分別代表橫豎撇捺折，可以輸入“12345”查到“札”。也可以輸入五筆字型的編碼查詢漢字，比如輸入“snn”查詢“扎”。"
	index = 5

	def 析(自, 列):
		字 = 列[1]
		wb = 列[自.index]
		return 字, wb
