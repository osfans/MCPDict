#!/usr/bin/env python3

from tables._數據庫 import 字表 as 表

class 字表(表):
	key = "ko_kor"
	dbkey = "kr"
	site = "Naver漢字辭典"
	url = "http://hanja.naver.com/hanja?q=%s"
	patches = {"冇": None}
