#!/usr/bin/env python3

from tables._數據庫 import 字表 as 表

class 字表(表):
	key = "ko_kor"
	dbkey = "kr"
	_lang = "朝鮮語"
	site = "Naver漢字辭典"
	url = "http://hanja.naver.com/hanja?q=%s"
	note = "來源：<a href=http://hanja.naver.com/>Naver漢字辭典</a><br>說明：括號前的讀音爲漢字本來的讀音，也是朝鮮的標準音，而括號內的讀音爲韓國應用<a href=http://zh.wikipedia.org/wiki/%E9%A0%AD%E9%9F%B3%E6%B3%95%E5%89%87>頭音法則</a>之後的讀音。"
	patches = {"冇": None}
