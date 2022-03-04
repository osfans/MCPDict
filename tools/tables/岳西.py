#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "gan_hy_yx"
	note = "來源：《安徽岳西方言的同音字匯》，“空島。”整理"
	tones = "21 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,324 3 2 上 ꜂,,52 5 3a 陰去 ꜄,33 6 3b 陽去 ꜅,213 7 4 入 ꜆"
	_file = "安徽省岳西方言同音字表*.tsv"
	simplified = 2

	def format(self, line):
		line = line.replace("*", "□")
		return line
