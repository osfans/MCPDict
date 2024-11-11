#!/usr/bin/env python3

from tables._表 import 表 as _表
import re, regex

class 表(_表):
	disorder = True
	sy = ""

	def parse(self, fs):
		name = str(self)
		yb = ""
		sd = ""
		if name in ("臨川","奉新宋埠"):
			sy, sd, hzs = fs[:3]
			if sy:
				self.sy = sy
			else:
				sy = self.sy
		elif name in ("平陰東阿",):
			sy, sd, _, hzs = fs[:4]
			if sy:
				self.sy = sy
			else:
				sy = self.sy
			yb = sy + sd
			hzs = self.normS(hzs)
		elif name in ("宜章巖泉",):
			sy, sd, hzs = fs[:3]
		elif name in ("望城",):
			sy, sd, hzs = fs[:3]
			hzs = hzs.replace("?", "□")
		elif name in ("無錫",):
			yb, hzs = fs[:2]
			hzs = hzs.replace("？", "?").replace(" ", "")
			hzs = self.normS(hzs)
		elif name in ("思南塘頭",):
			sy, sd, hzs = fs[:3]
			hzs = hzs.replace("∽", "~").replace(" ", "")
			hzs = self.normS(hzs)
		elif name in ("桃源薛家沖",):
			sy, sd, _, _, hzs = fs[:5]
			hzs = hzs.replace(")(", "；")
			hzs = self.normS(hzs)
		elif name in ("崇陽","通城塘湖","沅陵死客子話","宜章東風客家","新田毛里","資興南鄕", "婁底石井", "雙牌官話", "長沙黎圫","吉首", "懷化", "攸縣新市", "長沙星沙", "東安蘆洪市", "宜章東風", "道縣仙子腳"):
			sy, sd, _, hzs = fs[:4]
			hzs = hzs.replace(")(", "；")
			hzs = self.normS(hzs)
		elif name in ("江華河路口", "江華粟米塘", "全州黃沙河", "安仁新洲", "1935長沙", "長沙黃花"):
			sy, sd, hzs = fs[:3]
			hzs = self.normS(hzs)
		elif name in ("孝昌小河",):
			if not fs[0]: return
			groups = re.findall(r"^(.*?)(\d+) ?(.+)$", fs[0])
			if not groups: return
			sy, sd, hzs = groups[0]
			if not sy or not hzs: return
			sd = self.toneMaps[sd]
			hzs = hzs.strip().replace("{", "[").replace("}", "]")
		elif name in ("洞口",):
			yb, hzs = fs[:2]
			hzs = hzs.replace("{", "[").replace("}", "]")
		elif name in ("欽州正","道縣壽雁"):
			sy, sd, hzs = fs[:3]
			hzs = hzs.replace("{", "[").replace("}", "]")
		elif name in ("唐山-開平"):
			sy, sd, hzs = fs[:3]
			hzs = hzs.replace("{", "[").replace("}", "]")
			sd = self.toneMaps.get(sd, "0")
		elif name in ("汨羅沙溪",):
			sy, sd, hzs = fs[:3]
			hzs = self.normS(hzs)
			sd = sd.replace("42", "24")
			hzs = hzs.replace("☐", "□")
			sd = self.toneMaps.get(sd, "?")
		elif name in ("長沙雙江",):
			sy, sd, _, hzs = fs[:4]
			hzs = hzs.replace("、（", "₁（")
			hzs = self.normS(hzs)
		elif name in ("會同髙椅","會同靑朗"):
			sy, _, sd, hzs = fs[:4]
		elif name in ("湘鄕棋梓",):
			sy, sd, _, hzs = fs[:4]
		elif name in ("邵東斫曹","綏寧武陽","天柱江東"):
			sy, sd = fs[:2]
			hzs = "".join(fs[2:]).replace("\t", "").strip()
		elif name in ("吉安雲樓",):
			sy, sd, hzs = fs[:3]
			hzs = self.normS(hzs)
			sd = self.toneMaps[sd]
		elif name in ("揚州",):
			self.disorder = False
			self.simplified = 0
			yb, hzs = fs[:2]
			l = ""
			hzs = re.sub("(（.*?）)([？！%+，])?", "\\2\\1", hzs)
			for hz,c,js in re.findall(r"(.)([？！%+，])?(（[^）]*?（.*?）.*?）|（.*?）)?", hzs):
				if js: js = js[1:-1]
				p = ""
				if c == '+':
					p = "書"
					c = ""
				elif c == '？':
					c = "?"
				elif c == '！':
					c = "*"
				elif c == '%':
					p = "又音"
					c = ""
				elif c == '，':
					p = "外"
					c = ""
				if p:
					js = f"({p}){js}"
				l += f"{hz}{c}[{js}]"
			hzs = l
		elif len(fs) > 3 and fs[3]:
			_, sy, sd, hzs = fs[:4]
		else:
			sy, sd, hzs = fs[:3]
		if sd == "調號": return
		if not yb: yb = sy + sd
		l = list()
		hzs = self.normM(hzs)
		hzs = re.sub(r"(〚.*?〛)([-=])", "\\2\\1", hzs)
		for hz, c, o, js in re.findall(r"(.)([-=*?]?)([₀-₉0-9]?)(〚.*?〛)?", hzs):
			if js: js = js[1:-1]
			js = o + js
			l.append((hz, yb + c, js))
		return l