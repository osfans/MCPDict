#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_oj_rats"
	note = "版本：2021-11-26<br>來源：浙南甌語(顏逸明)，有一定的修改，轉錄者落橙"
	tones = "55 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,35 3 2a 陰上 ꜂,24 4 2b 陽上 ꜃,52 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,323 7 4a 陰入 ꜆,212 8 4b 陽入 ꜇"
	_file = "浙南瓯语(颜逸明)-瑞安陶山-字表-横.tsv"

	def parse(self, fs):
		hz, sm, ym, sd, js, bz = fs[:6]
		sd = sd.strip("[]")
		yb = sm + ym + sd
		js = (js + " " +bz).strip()
		return hz, yb, js

