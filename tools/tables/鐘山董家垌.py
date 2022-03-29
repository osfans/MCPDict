#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "xxx_zsdjd"
	tones = "35 1 1a 陰平 ꜀,213 2 1b 陽平 ꜁,42 3 2a 陰上 ꜂,33 4 2b 陽上 ꜃,51 5 3a 陰去 ꜄,31 6 3b 陽去 ꜅,44 7 4 入 ꜆"
	_file = "钟山董家垌土话.tsv"
	simplified = 2
