#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_oj_cnpm"
	_file = "苍南蒲门瓯语方言岛*.tsv"
	_lang = "蒼南蒲門甌語方言島"
	note = "版本：V2.0 (2021-10-19)<br>來源：鄭張尚芳調查資料、陳玉燕《浙南蒲門甌語方言島語音研究》附錄同音字彙，轉錄者<u>落橙</u>、<u>阿纓</u>"
	tones = "44 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,45 3 2a 陰上 ꜂,24 4 2b 陽上 ꜃,42 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,323 7 4a 陰入 ꜆,212 8 4b 陽入 ꜇"
	toneValues = {'44':1,'31':2,'45':3,'24':4,'42':5,'22':6,'323':7,'212':8}

	def parse(self, fs):
		hz,yb,sd = fs[:3]
		if not yb: return
		sd = str(self.toneValues[sd])
		yb += sd
		return hz, yb
