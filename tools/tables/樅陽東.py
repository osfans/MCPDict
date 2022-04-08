#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_jh_hx_aq_zyd"
	_file = "安徽省枞阳东部方言同音字表*.tsv"
	
	def format(self, line):
		line = line.replace("*", "□").replace("(","（").replace(")","）").replace("（","{").replace("）","}").replace("、","/")
		line = re.sub('^""	', "", line)
		if "调值" not in line:
			line = re.sub('^"(#[^ ]*) .*?"', "\\1", line)
			line = re.sub('^("[^ ]+) .*?"', '\\1"', line)
			line = re.sub('^"(.*?)/.*?"', '"\\1"', line)
			line = line.replace("윽", "")
		return line
