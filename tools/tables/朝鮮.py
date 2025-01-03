#!/usr/bin/env python3

from tables._數據庫 import 表 as _表

class 表(_表):
	鍵 = "kr"
	網站 = "Naver漢字辭典"
	網址 = "http://hanja.naver.com/hanja?q=%s"
	補丁 = {"冇": None}
