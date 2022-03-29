#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_hzhx"
	tones = "213 1 1a 陰平 ꜀,45 2 1b 陽平 ꜁,24 3 2 上 ꜂,,42 5 3 去 ꜄"
	_file = "洪泽洪祥.tsv"
	simplified = 2
