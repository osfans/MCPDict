#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):

	def parse(self, fs):
		hz,jt,py,js = fs
		sd = py[-1]
		py = py[:-1]
		py = py.replace("w","u").replace("uu", "u")
		py = re.sub("^(ts|tsh|s|z)i", "\\1ɿ", py)
		py = re.sub("^y(?=[^u])", "i", py).replace("ii","i")
		py = re.sub("^(c|ch|sh|zh)u", "\\1yu", py)
		py = py.replace("iu", "iou").replace("ui", "uei").replace("yun", "yn").replace("un", "uen")
		yb = py.replace("ou", "əu").replace("ao", "au").replace("ang", "ã").replace("an", "ẽ").replace("yu", "y")
		yb = re.sub("^h", "x", yb).replace("gh", "ɣ").replace("sh", "ɕ").replace("zh", "ʑ").replace("h", "ʰ")\
			.replace("ts", "ts").replace("c", "tɕ").replace("ng", "ŋ")
		yb = yb + sd
		return hz, yb, js
