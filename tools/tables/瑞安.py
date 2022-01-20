#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "wuu_oj_ra"
	note = "說明：主要參考《瑞安話語音研究（陳海芳）》，有一定的修改，轉錄者<u>落橙</u>》"
	tones = "55 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,35 3 2a 陰上 ꜂,24 4 2b 陽上 ꜃,52 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,434 7 4a 陰入 ꜆,323 8 4b 陽入 ꜇"
	_file = "瑞安话语音研究*.tsv"
	simplified = 2
