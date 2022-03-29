#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "xxx_jcby"
	_file = "剑川白语核心词表.tsv"
	tones = "55 1 1a 陰平 ꜀,21 2 1b 陽平 ꜁,33 3 2 上 ꜂,,42 5 3a 陰去 ꜄,31 6 3b 陽去 ꜅,44 7 4 入 ꜆,,35 9 5 例外 "
	simplified = 2

	def parse(self, fs):
		hz, sy, sd, js = fs[:4]
		yb = sy + sd
		return hz, yb, js
