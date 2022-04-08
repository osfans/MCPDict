#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "xxx_jcby"
	_file = "剑川白语核心词表.tsv"

	def parse(self, fs):
		hz, sy, sd, js = fs[:4]
		yb = sy + sd
		return hz, yb, js
