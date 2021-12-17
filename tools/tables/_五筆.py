#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	_color = "#1E90FF"
	_file = "wb.csv"
	note = "來源：<a href=https://github.com/CNMan/UnicodeCJK-WuBi>五筆字型Unicode CJK超大字符集編碼數據庫</a><br>說明：12345分別代表橫豎撇捺折，可以輸入“12345”查到“札”。也可以輸入五筆字型的編碼查詢漢字，比如輸入“snn”查詢“扎”。"
	index = 5
	hasHead = False

	def parse(self, fs):
		hz = fs[1]
		wb = fs[self.index]
		return hz, wb
