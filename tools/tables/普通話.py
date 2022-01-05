#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cmn_"
	note = "版本：2021-08-23<br>來源：漢語大字典、<a href=https://www.zdic.net/>漢典</a>、<a href=http://yedict.com/>字海</a>、<a href=https://www.moedict.tw/>萌典</a><br>說明：灰色讀音來自<a href=https://www.moedict.tw/>萌典</a>。可使用漢語拼音、注音符號查詢漢字。在輸入漢語拼音時，可以用數字1、2、3、4代表聲調，放在音節末尾，“?”可代表任何聲調；字母ü可用v代替。例如查詢普通話讀lüè的字時可輸入lve4。在輸入注音符號時，聲調一般放在音節末尾，但表示輕聲的點（˙）既可以放在音節開頭，也可以放在音節末尾，例如“的”字的讀音可拼作“˙ㄉㄜ”或“ㄉㄜ˙”。"
	tones = "55 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,214 3 2 上 ꜂,51 5 3 去 ꜄"
	site = "字海"
	url = "http://yedict.com/zscontent.asp?uni=%2$s"
	isYb = False

	def parse(self, fs):
		if fs[0].startswith("#"): return
		l = list()
		hzs,py = fs[:2]
		for hz in hzs:
			for yb in py.split(","):
				l.append((hz, yb))
		return l

