#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "hsn_cayi_ss"
	note = "版本：2021-12-11<br>來源：王福堂.韶山方言同音字汇两种[J].方言,2017(3):257-278.，轉錄者：跳跳老鼠"
	tones = "33 1 1a 陰平 ꜀,113 2 1b 陽平 ꜁,42 3 2 上 ꜂,,55 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅,24 7 4 入 ꜆"

	def parse(self, fs):
		hz, yb, js = fs[:3]
		c = ""
		if yb[-1] in "-=":
			c = "白" if yb[-1] == "-" else "文"
			if c and js: c = "(%s)" % c
			yb = yb.rstrip("-=")
			js = c + js
		return hz, yb, js

