#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cdo_cnqk"
	_file = "钱库蛮话*.tsv"
	_color = "#DB7093,#0000FF"
	note = "更新：2021-10-14<br>來源：<u>阿纓</u>"
	tones = "44 1 1a 陰平 ꜀,213 2 1b 陽平 ꜁,45 3 2 上 ꜂,,41 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,4 7 4a 陰入 ꜆,2 8 4b 陽入 ꜇"
	simplified = 2

	def parse(self, fs):
		sm,ym,sd,hz,js = fs[:5]
		if sm == "零": sm = ""
		yb = sm + ym + sd
		return hz, yb, js
