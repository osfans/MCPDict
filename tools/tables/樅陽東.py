#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_jh_hx_aq_zyd"
	tones = "31 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,335 3 2 上 ꜂,,53 5 3 去 ꜄,,5 7 4 入 ꜆"
	_file = "安徽省枞阳东部方言同音字表*.tsv"
	simplified = 2
	
	def format(self, line):
		line = line.replace("*", "□").replace("(","（").replace(")","）").replace("（","{").replace("）","}").replace("、","/")
		line = re.sub('^""	', "", line)
		if "调值" not in line:
			line = re.sub('^"(#[^ ]*) .*?"', "\\1", line)
			line = re.sub('^("[^ ]+) .*?"', '\\1"', line)
		return line
