#!/usr/bin/env python3

import re
from tables._表 import 表 as _表

class 表(_表):
	def 析(自, 列):
		if "[" not in 列[0]: return
		l = list()
		行 = 列[0].replace(" ", "")
		for 音, 字組 in re.findall(r"\[(.*?)\]([^[]+)", 行):
			音 = 自.轉調類(音)
			字組 = 自.normS(字組, "（\\1）")
			for 字, js in re.findall("(.)(（.*?）)?", 字組):
				l.append((字, 音, js.strip("（）")))
		return l
