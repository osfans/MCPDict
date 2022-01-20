#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "gan_lcsdd"
	_file = "臨川上頓渡同音字彙.tsv"
	note = "來源：Pekkhak（啵啵）整理自《撫州方言研究》<br>說明：資料記錄的是臨川上頓渡口音，並非老城區口音，其與老城區口音最大的區別在於陰平和陽去尚未合流"
	tones = "11 1 1a 陰平 ꜀,24 2 1b 陽平 ꜁,35 3 2 上 ꜂,,42 5 3a 陰去 ꜄,33 6 3b 陽去 ꜅,2 7 4a 陰入 ꜆,5 8 4b 陽入 ꜇"
	simplified = 2
