#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	site = "韻典網（上古音系）"
	url = "https://ytenx.org/dciangx/dzih/%s"
	note = "來源：<a href=https://ytenx.org/dciangx/>韻典網上古音鄭張尚芳擬音</a><br>說明：在擬音後面的括號中注明了《上古音系》中的反切、聲符、韻部。/後的讀音爲上古後期擬音。"
	ybTrimSpace = False
	isYb = False

	def parse(self, fs):
		hz = fs[0]
		js = fs[16]
		yb = ("%s%s (%s%s切 %s聲 %s%s)"%(fs[12], f"/{fs[13]}" if fs[13] else "", fs[7],fs[8],fs[9],fs[10],fs[11]))
		return hz, yb, js
