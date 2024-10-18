#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	disorder = True
	sy = ""

	def parse(self, fs):
		name = str(self)
		if name in ("臨川","奉新宋埠"):
			sy, sd, hzs = fs[:3]
			if sy:
				self.sy = sy
			else:
				sy = self.sy
		elif name in ("宜章巖泉",):
			sy, sd, hzs = fs[:3]
		elif name in ("江華河路口"):
			sy, sd, hzs = fs[:3]
			hzs = hzs.replace("(", "[").replace(")", "]").replace("（", "[").replace("）", "]")
		elif name in ("欽州正"):
			sy, sd, hzs = fs[:3]
			hzs = hzs.replace("{", "[").replace("}", "]")
		elif name in ("平陰東阿",):
			sy, sd, _, hzs = fs[:4]
			if sy:
				self.sy = sy
			else:
				sy = self.sy
			yb = sy + sd
			hzs = hzs.replace("¨", "□")\
				.replace("(", "[").replace(")", "]").replace("（", "[").replace("）", "]")
		elif name in ("長沙雙江",):
			sy, sd, _, hzs = fs[:4]
			hzs = re.sub("[₁₂₃]", "", hzs)
			hzs = hzs.replace("[", "［").replace("]", "］").replace("（", "[").replace("）", "]").replace("(", "[").replace(")", "]")
		elif name in ("會同高椅","會同青朗"):
			sy, _, sd, hzs = fs[:4]
		elif name in ("湘鄕棋梓",):
			sy, sd, _, hzs = fs[:4]
		elif len(fs) > 3 and fs[3]:
			_, sy, sd, hzs = fs[:4]
		else:
			sy, sd, hzs = fs[:3]
		if sd == "調號": return
		yb = sy + sd
		l = list()
		hzs = re.sub(r"(\[.*?\])([-=])", "\\2\\1", hzs)
		for hz, c, js in re.findall(r"(.)([-=]?)(\[[^[]]*?\[[^[]]*?\][^[]]*?\]|\[.*?\])?", hzs):
			if js: js = js[1:-1]
			if hz == "~": hz = "□"
			l.append((hz, yb + c, js))
		return l

