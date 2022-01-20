#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._縣志 import 字表 as 表

class 字表(表):
	key = "xxx_glps"
	_file = "桂林平山土话同音字表.tsv"
	note = "來源：清竮塵整理自蘇彥湄《桂林平山土話語音研究》"
	tones = "51 1 1a 陰平 ꜀,33 2 1b 陽平 ꜁,43 3 2a 陰上 ꜂,24 4 2b 陽上 ꜃,35 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅,5 7 4 入 ꜆"
	simplified = 2
	#disorder = True

