#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "hsn_cs"
	_file = "長沙話字表 *.tsv"
	note = "版本：1.4.2.2 (2021-12-17)<br>來源：《湘音檢字》正音館"
	tones = "334 1 1a 陰平 ꜀,213 2 1b 陽平 ꜁,41 3 2 上 ꜂,,45 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅,14 7 4 入 ꜆"

	def parse(self, fs):
		#"拼音"	"寬式IPA"	"調號"	"字甲"	"字乙"	"釋義"
		_, yb, sd, hz, hzb, js = fs[:6]
		yb = yb + sd
		l = list()
		l.append((hz, yb, js))
		if hz != hzb and hzb:
			l.append((hzb, yb, js))
		return l


