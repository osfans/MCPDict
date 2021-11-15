#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	_file = "粤西闽语方言字表2.5（繁体）(文锋).tsv"
	note = "版本：V2.5 (2021-11-20)<br>來源：<u>Kiattan</u>"

	def parse(self, fs):
		if len(fs) < 6: return
		hz = fs[0]
		ybs = fs[self.index]
		if not ybs or ybs.startswith("—"): return
		js = hz[1:] if len(hz)>1 else ""
		hz = hz[0]
		l = list()
		for _yb in ybs.split("/"):
			_yb = _yb.strip()
			sd = re.findall("\d+", _yb)[0]
			yb = _yb.replace(sd, str(self.toneValues[sd]))
			js = js.strip("（）")
			l.append((hz, yb, js))
		return l

