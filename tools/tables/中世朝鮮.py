#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "ko_okm"
	_lang = "中世紀朝鮮語"
	_color = "#8B0000,#4D4D4D"
	#https://github.com/nk2028/sino-korean-readings/blob/main/woosun-sin.csv
	_file = "woosun-sin.csv"
	note = "來源：<a href=https://github.com/nk2028/sino-korean-readings>韓國漢字音歷史層次研究</a>"

	def parse(self, fs):
		hz = fs[0]
		py = "".join(fs[1:4])
		return hz, py
