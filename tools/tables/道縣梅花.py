#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "xxx_dxmh"
	tones = "51 1 1a 陰平 ꜀,21 2 1b 陽平 ꜁,55 3 2a 陰上 ꜂,33 4 2b 陽上 ꜃,44 5 3 去 ꜄,,35 7 4 入 ꜆"
	_file = "道县梅花土话.tsv"
	simplified = 2

	#!西官陰平藉詞@西官陽平藉詞$西官上聲藉詞%西官去聲藉詞
	def format(self, line):
		line = re.sub("(!)(?!{)","{西官陰平借詞}",line)
		line = line.replace("!{","{(西官陰平借詞)")
		line = re.sub("(@)(?!{)","{西官陽平借詞}",line)
		line = line.replace("@{","{(西官陽平借詞)")
		line = re.sub("(\$)(?!{)","{西官上聲借詞}",line)
		line = line.replace("${","{(西官上聲借詞)")
		line = re.sub("(%)(?!{)","{西官去聲借詞}",line)
		line = line.replace("%{","{(西官去聲借詞)")
		return line
