#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	disorder = True

	def parse(self, fs):
		if len(fs) < 4: return
		if fs[2].isdigit():
			hz, sy, tv, js = fs[:4]
		elif fs[3].isdigit():
			hz, sm, ym, tv, js = fs[:5]
			sy = sm + ym
		elif str(self) == "滁州":
			_, hz, sm, ym, tv, js = fs[:6]
			sy = sm + ym
		elif fs[4].isdigit():
			_, hz, sm, ym, tv, _, js = fs[:7]
			sy = sm + ym
		elif fs[2] and fs[2][-1].isdigit():
			hz, _, sy, js = fs[:4]
			tv = re.findall("\d+$", sy)[0]
			sy = sy.rstrip("012345")
		else:
			return
		if not hz or tv == "调": return
		if tv:
			if tv not in self.toneMaps:
				#print(tv)
				sd = "?"
			else:
				sd = self.toneMaps[tv]
			if sy[-1] in "ptkʔ" and tv + "0" in self.toneMaps:
				sd = self.toneMaps[tv + "0"]
		else: sd = ""
		yb = sy + sd
		return hz, yb, js

