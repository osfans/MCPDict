#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cmn_jh_tt_rgrc"
	tones = ",35 2 1b 陽平 ꜁,213 3 2 上 ꜂,,44 5 3a 陰去 ꜄,31 6 3b 陽去 ꜅,44 7 4a 陰入 ꜆,35 8 4b 陽入 ꜇"
	_file = "如皋如城同音字表.tsv"
	simplified = 2

