#!/usr/bin/env python3

from tables._縣志 import 字表 as 表

class 字表(表):
	key = "gan_ygyt"
	note = "說明：餘干方言入聲韻在發了塞音尾之後，經過短暫的休止，又有一個同部位的鼻音，且入聲韻的兩個部分都有自己的調值。"
	tones = "33 1 1a 陰平 ꜀,25 2 1b 陽平 ꜁,213 3 2 上 ꜂,,45 5 3a 陰去 ꜄,23 6 3b 陽去 ꜅,104 7 4a 陰入 ꜆,101 8 4b 陽入 ꜇"
	_file = "餘干玉亭同音字彙*.tsv"
	simplified = 2
