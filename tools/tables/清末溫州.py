#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_oj_wz_ltc"
	_file = "清末温州*.tsv"
	_color = "#0000FF,#4D4D4D"
	note = "版本：V2.1 (2021-10-21)<br>來源：<u>阿纓</u>轉錄<br>參考文獻：1.《清末溫州方言音系研究》，張雪，2015；2.《溫州方言入門》，P.H.S.蒙哥馬利,1893"
	tones = "44 1 1a 陰平 ꜀,331 2 1b 陽平 ꜁,45 3 2a 陰上 ꜂,34 4 2b 陽上 ꜃,42 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,223 7 4a 陰入 ꜆,112 8 4b 陽入 ꜇"

	def parse(self, fs):
		if len(fs) < 7: return
		_,hz,yb,_,_,_,js = fs[:7]
		return hz, yb, js
