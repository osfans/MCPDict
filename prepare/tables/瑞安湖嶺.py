#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_oj_rahl"
	note = "版本：2021-12-11<br>來源：《瑞安湖嶺方言音系（太田斋）》，轉錄者落橙，校對者阿纓"
	tones = "44 1 1a 陰平 ꜀,31 2 1b 陽平 ꜁,35 3 2a 陰上 ꜂,13 4 2b 陽上 ꜃,53 5 3a 陰去 ꜄,212 6 3b 陽去 ꜅,424 7 4a 陰入 ꜆,323 8 4b 陽入 ꜇"
	_file = "瑞安湖岭字表-20211211.tsv"

	def parse(self, fs):
		hz, sm, ym, sd, js = fs[1], fs[5], fs[6], fs[10], fs[4]
		yb = sm + ym + sd
		return hz, yb, js

