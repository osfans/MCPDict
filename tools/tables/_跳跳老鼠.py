#!/usr/bin/env python3

from tables._表 import 表 as _表
import re, regex

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
		elif name in ("望城",):
			sy, sd, hzs = fs[:3]
			hzs = hzs.replace("?", "□")
		elif name in ("宜章巖泉",):
			sy, sd, hzs = fs[:3]
		elif name in ("思南塘頭",):
			sy, sd, hzs = fs[:3]
			hzs = hzs.replace("(", "（").replace(")", "）").replace("∽", "~").replace(" ", "")
			hzs = re.sub("([₁-₃])(（)", "\\2\\1", hzs)
			hzs = re.sub("([^（])([₁-₃])", "\\1（\\2）", hzs)
			hzs = regex.sub("（((?>[^（）]+|(?R))*)）", "[\\1]", hzs)
		elif name in ("江華河路口", "江華粟米塘", "全州黃沙河", "安仁新洲", "1935長沙"):
			sy, sd, hzs = fs[:3]
			hzs = hzs.replace("(", "（").replace(")", "）")
			hzs = regex.sub("（((?>[^（）]+|(?R))*)）", "[\\1]", hzs)
		elif name in ("孝昌小河",):
			if not fs[0]: return
			groups = re.findall(r"^(.*?)(\d+) ?(.+)$", fs[0])
			if not groups: return
			sy, sd, hzs = groups[0]
			if not sy or not hzs: return
			sd = self.toneMaps[sd]
			hzs = hzs.strip().replace("{", "[").replace("}", "]")
		elif name in ("欽州正",):
			sy, sd, hzs = fs[:3]
			hzs = hzs.replace("{", "[").replace("}", "]")
		elif name in ("唐山-開平"):
			sy, sd, hzs = fs[:3]
			hzs = hzs.replace("{", "[").replace("}", "]")
			sd = self.toneMaps.get(sd, "0")
		elif name in ("汨羅沙溪",):
			sy, sd, hzs = fs[:3]
			hzs = hzs.replace("(", "（").replace(")", "）")
			hzs = regex.sub("（((?>[^（）]+|(?R))*)）", "[\\1]", hzs)
			sd = sd.replace("42", "24")
			sd = self.toneMaps.get(sd, "?")
		elif name in ("平陰東阿",):
			sy, sd, _, hzs = fs[:4]
			if sy:
				self.sy = sy
			else:
				sy = self.sy
			yb = sy + sd
			hzs = hzs.replace("¨", "□").replace("(", "（").replace(")", "）")
			hzs = regex.sub("（((?>[^（）]+|(?R))*)）", "[\\1]", hzs)
		elif name in ("長沙雙江",):
			sy, sd, _, hzs = fs[:4]
			hzs = re.sub("[₁₂₃]", "", hzs)
			hzs = hzs.replace("[", "［").replace("]", "］").replace("（", "[").replace("）", "]").replace("(", "[").replace(")", "]")
		elif name in ("會同髙椅","會同靑朗"):
			sy, _, sd, hzs = fs[:4]
		elif name in ("湘鄕棋梓",):
			sy, sd, _, hzs = fs[:4]
		elif name in ("崇陽","通城塘湖","沅陵死客子話","宜章東風","新田毛里","資興南鄕", "婁底石井", "雙牌官話", "長沙黎圫"):
			sy, sd, _, hzs = fs[:4]
			hzs = hzs.replace("(", "（").replace(")", "）")
			hzs = regex.sub("（((?>[^（）]+|(?R))*)）", "[\\1]", hzs)
		elif name in ("邵東斫曹","綏寧武陽","天柱江東"):
			sy, sd = fs[:2]
			hzs = "".join(fs[2:]).replace("\t", "").strip()
		elif name in ("吉安雲樓",):
			sy, sd, hzs = fs[:3]
			hzs = hzs.replace("(", "（").replace(")", "）").replace("（", "[").replace("）", "]")
			hzs = hzs.replace("₂", "2")
			hzs = re.sub(r"(\d)(\[)", "\\2\\1", hzs)
			sd = self.toneMaps[sd]
		elif len(fs) > 3 and fs[3]:
			_, sy, sd, hzs = fs[:4]
		else:
			sy, sd, hzs = fs[:3]
		if sd == "調號": return
		yb = sy + sd
		l = list()
		hzs = hzs.replace("[", "［").replace("]", "］")
		hzs = re.sub(r"(［.*?］)([-=])", "\\2\\1", hzs)
		for hz, c, js in re.findall(r"(.)([-=]?)(［[^［］]*?［[^［］]*?］[^［］]*?］|［.*?］)?", hzs):
			if js: js = js[1:-1]
			if hz in "☐": hz = "□"
			l.append((hz, yb + c, js))
		return l

