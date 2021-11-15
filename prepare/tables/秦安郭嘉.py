#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "cmn_zho_qagj"
	note = "版本：1.1 (2021-11-07)<br>來源：<u>清竮塵</u>整理自李小潔《甘肅秦安縣郭嘉方言語音研究》"
	tones = "113 1 1 平 ꜀,,53 3 2 上 ꜂,,44 5 3 去 ꜄"
	_file = "秦安郭嘉同音字表*.tsv"
