#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "gan_gayx"
	note = "更新：2021-11-14<br>來源：Pekkhak（啵啵）整理自顏森《高安（老屋周家）方言的语音系统》<br>說明：這份資料記錄的並非高安市區方言，其面貌與高安市區方言存在一定的差異"
	tones = "55 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,42 3 2 上 ꜂,,33 5 3a 陰去 ꜄,11 6 3b 陽去 ꜅,3 7 4a 陰入 ꜆,1 8 4b 陽入 ꜇"
	_file = "高安楊墟同音字彙*.tsv"
	simplified = 2
