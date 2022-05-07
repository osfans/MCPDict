#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):

	def parse(self, fs):
		hz,yb = fs[:2]
		sd = re.findall("[0-9]+$", yb)
		yb = yb.rstrip("012345")
		if sd:
			tv = sd[0]
			sd = self.toneMaps[tv]
			if yb[-1] in "ptkʔ" and tv + "0" in self.toneMaps:
				sd = self.toneMaps[tv + "0"]
		else:
			sd = ""
		if sd: yb = yb + sd
		return hz, yb

