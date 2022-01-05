#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_xn_xzzp"
	note = "版本：1.1 (2021-10-14)<br>來源：<u>清竮塵</u>整理自唐七元、仇浩揚《廣西象州中平官話同音字彙》"
	tones = "44 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,54 3 2 上 ꜂,,35 5 3a 陰去 ꜄,213 6 3b 陽去 ꜅"
	_file = "象州中平同音字表*.tsv"
	simplified = 2
