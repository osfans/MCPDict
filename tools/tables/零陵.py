#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "cmn_xn_yzll"
	_lang = "永州零陵話"
	note = "來源：<a href=https://github.com/shinzoqchiuq/yongzhou-homophony-syllabary>永州官話同音字表</a>、《湖南省志·方言志》<br>說明：本同音字表描寫的是屬於山北片區的永州零陵區口音，整理自《湖南省志·方言志》，有脣齒擦音 /f/，無全濁塞擦音 /dz/ 和 /dʒ/，「彎」「汪」不同韻，區分陰去和陽去"
	tones = "13 1 1a 陰平 ꜀,33 2 1b 陽平 ꜁,55 3 2 上 ꜂,24 5 3a 陰去 ꜄,324 6 3b 陽去 ꜅"
	_file = "永州官話同音字表.tsv"

	def parse(self, fs):
		hz,jt,py,js = fs
		sd = py[-1]
		py = py[:-1]
		py = py.replace("w","u").replace("uu", "u")
		py = re.sub("^(ts|tsh|s|z)i", "\\1ɿ", py)
		py = re.sub("^y(?=[^u])", "i", py).replace("ii","i")
		py = re.sub("^(c|ch|sh|zh)u", "\\1yu", py)
		py = py.replace("iu", "iou").replace("ui", "uei").replace("yun", "yn").replace("un", "uen")
		yb = py.replace("ou", "əu").replace("ao", "au").replace("ang", "ã").replace("an", "ẽ").replace("yu", "y")
		yb = re.sub("^h", "x", yb).replace("gh", "ɣ").replace("sh", "ɕ").replace("zh", "ʑ").replace("h", "ʰ")\
			.replace("ts", "ts").replace("c", "tɕ").replace("ng", "ŋ")
		yb = yb + sd
		return hz, yb, js
