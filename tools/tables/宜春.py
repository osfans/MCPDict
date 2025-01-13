#!/usr/bin/env python3

import re
from tables._表 import 表 as _表

class 表(_表):
	sms = None
	def 析(自, 列):
		if 列[0] == "韵尾":
			自.sms = 列[3:]
			return
		ym = 列[2]
		l = list()
		for i, cell  in enumerate(列[3:]):
			if "【" not in cell: cell += "【5】"
			for 字組, sd in re.findall(r"(.*?)【(.*?)】", cell):
				yb = 自.sms[i] + ym + sd
				yb = yb.strip("零")
				for 字 in 字組:
					l.append((字, yb))
		return l
