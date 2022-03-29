#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_th_pl_jy"
	_file = "江阴jiangyin.tsv"
	tones = "53 1 1a 陰平 ꜀,13 2 1b 陽平 ꜁,45 3 2 上 ꜂,,423 5 3a 陰去 ꜄,213 6 3b 陽去 ꜅,55 7 4a 陰入 ꜆,24 8 4b 陽入 ꜇"

	def parse(self, fs):
		return fs[3], fs[6]
