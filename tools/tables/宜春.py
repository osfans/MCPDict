#!/usr/bin/env python3

import re
from tables._表 import 表 as _表

class 表(_表):
	sms = None
	def parse(self, fs):
		if fs[0] == "韵尾":
			self.sms = fs[3:]
			return
		ym = fs[2]
		l = list()
		for i, cell  in enumerate(fs[3:]):
			if "【" not in cell: cell += "【5】"
			for hzs, sd in re.findall(r"(.*?)【(.*?)】", cell):
				yb = self.sms[i] + ym + sd
				yb = yb.strip("零")
				for hz in hzs:
					l.append((hz, yb))
		return l
