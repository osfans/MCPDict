#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_jh_tt_yz"
	tones = "21 1 1a 陰平 ꜀,45 2 1b 陽平 ꜁,214 3 2 上 ꜂,,33 5 3 去 ꜄,,3 7 4a 陰入 ꜆,45 8 4b 陽入 ꜇"
	_file = "扬中.tsv"
	simplified = 2

