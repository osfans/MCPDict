#!/usr/bin/env python3

from tables._縣志 import 表 as _表
import re

class 表(_表):

	def format(self, line):
		line = re.sub("^.*?\t", "", line)
		line = line.replace("（白）","-").replace("（文）","=").replace("(", "{").replace("〔", "{").replace("（","{").replace(")", "}").replace("）", "}")
		return line

