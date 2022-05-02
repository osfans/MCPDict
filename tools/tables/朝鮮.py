#!/usr/bin/env python3

from tables._數據庫 import 表 as _表

class 表(_表):
	dbkey = "kr"
	site = "Naver漢字辭典"
	url = "http://hanja.naver.com/hanja?q=%s"
	patches = {"冇": None}
