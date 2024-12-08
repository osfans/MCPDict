#!/usr/bin/env python3

from tables._表 import 表 as _表
import re, regex

class 表(_表):
	disorder = True
	sm = ""
	ym = ""
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
		elif name in ("景寧東坑",):
			if len(fs) == 1 or not fs[1].strip():
				self.ym = fs[0].strip()
				return
			yb, hzs = fs[:2]
			yb = yb.strip().replace(" ", "")
			yb = re.sub("^[無øØ]", "0", yb)
			digits = "12345678"
			if yb in digits:
				yb = self.sy + yb
			elif self.ym:
				self.sm = yb.rstrip(digits)
				self.sy = self.sm + self.ym
				yb = self.sy + yb[-1]
			else:
				self.sy = yb.rstrip(digits)
			hzs = hzs.replace("，", "(文)").replace("。", "(白)").replace("！", "(小稱)").replace(".", "(又)").replace("？", "(存疑)").replace(")（", " ")
			hzs = self.normS(hzs)
		elif name in ("平陰東阿",):
			sy, sd, _, hzs = fs[:4]
			if sy:
				self.sy = sy
			else:
				sy = self.sy
			yb = sy + sd
			hzs = self.normS(hzs)
		elif name in ("宜章巖泉","望城"):
			sy, sd, hzs = fs[:3]
			hzs = hzs.replace("?", "□")
		elif name in ("天台東鄕",):
			if not fs[0]: return
			g = re.findall(r"^(.*?)(\d)(.+)$", fs[0])
			if not g:
				ym = fs[0].strip()
				if ym:
					self.ym = ym
				return
			sm, sd, hzs = g[0]
			if sm:
				self.sm = sm
			yb = (self.sm + self.ym + sd).lstrip("q")
			hzs = self.normS(hzs)
		elif name in ("成都"):
			fs = "".join(fs).strip()
			if not fs: return
			g = re.findall(r"^(.*?)\[(\d+)\](.+)$", fs)
			if not g:
				ym = fs
				if ym:
					self.ym = ym
				return
			sm, sd, hzs = g[0]
			sd = self.toneMaps.get(sd, "?")
			if sm:
				self.sm = sm
			yb = (self.sm + self.ym + sd).lstrip("q")
			hzs = self.normS(hzs)
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
		elif name in ("通東呂四"):
			sy, sd, _, hzs = fs[:4]
			hzs = hzs.replace(")(", "；")
			hzs = re.sub(r"(\d)([-=])", "\\2\\1", hzs)
			hzs = self.normS(hzs)
		elif name in ("通城",):
			_, sy, sd, hzs = fs[:4]
			sd = sd.strip("[]")
			hzs = self.normG(hzs, "[\\1]")
		elif name in ("灌陽新派",):
			sy, sd, hzs = fs[:3]
			hzs = self.normG(hzs, "[\\1]")
		elif name in ("江華河路口", "江華粟米塘", "全州黃沙河", "安仁新洲", "1935長沙", "長沙黃花", "瀏陽鎭頭"):
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
			hzs = self.normG(hzs, "[\\1]")
		elif name in ("欽州正","道縣壽雁", "江永桃川", "桂陽敖泉"):
			sy, sd, hzs = fs[:3]
			hzs = self.normG(hzs, "[\\1]")
		elif name in ("唐山-開平"):
			sy, sd, hzs = fs[:3]
			hzs = self.normG(hzs, "[\\1]")
			sd = self.toneMaps.get(sd, "0")
		elif name in ("太原"):
			sm, ym, sd, hzs = fs[:4]
			hzs = self.normG(hzs, "[\\1]")
			sd = self.toneMaps.get(sd[2:], "0")
			yb = sm + ym + sd
		elif name in ("汨羅沙溪",):
			sy, sd, hzs = fs[:3]
			hzs = self.normS(hzs)
			sd = sd.replace("42", "24")
			sd = self.toneMaps.get(sd, "?")
		elif name in ("長沙雙江",):
			sy, sd, _, hzs = fs[:4]
			hzs = hzs.replace("、（", "₁（")
			hzs = self.normS(hzs)
		elif name in ("會同髙椅","會同靑朗", "臨武"):
			sy, _, sd, hzs = fs[:4]
		elif name in ("1884甯城",):
			_,_,hzs,sm,ym = fs[:5]
			yb = sm + ym
			if not yb: return
		elif name in ("湘鄕棋梓",):
			sy, sd, _, hzs = fs[:4]
		elif name in ("邵東斫曹","綏寧武陽","天柱江東"):
			sy, sd = fs[:2]
			hzs = "".join(fs[2:]).replace("\t", "").strip()
		elif name in ("吉安雲樓",):
			sy, sd, hzs = fs[:3]
			hzs = self.normS(hzs)
			sd = self.toneMaps[sd]
		elif name in ("濟南", "西安", "杭州"):
			sy, sd, hzs = fs[1:4]
			sd = re.sub(r"^[^\d]+", "", sd)
			sd = self.toneMaps.get(sd, "")
			hzs = hzs.replace(", ", "").replace(", ", "")
			hzs = self.normG(hzs, "〚\\1〛")
		elif name in ("梅縣", ):
			g = re.findall(r"^([^\d]*\d+)(.*?)$", "".join(fs))
			if g:
				yb, hzs = g[0]
				hzs = self.normG(hzs, "〚\\1〛").strip()
				yb = self.dz2dl(yb)
			else:
				return
		elif name in ("揚州",):
			self.disorder = False
			self.simplified = 0
			yb, hzs = fs[:2]
			hzs = self.normS(hzs, "（\\1）").replace("?", "？")
			l = ""
			if "（" in yb:
				note = re.search(r"（.*?）", yb).group()
				yb = yb.replace(note, "")
				note = note.replace("？", "存疑")
			else:
				note = ""
			marks = "？！%+，。"
			hzs = re.sub("(（.*?）)([？！%+，。])?", "\\2\\1", hzs)
			for hz,c,js in re.findall(r"(.)([？！%+，。])?(（[^）]*?（.*?）.*?）|（.*?）)?", hzs):
				if js:
					js = js[1:-1]
					if js[0] in marks and not c:
						c = js[0]
						js = js[1:]
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
				elif c == '。':
					p = "非本字正字"
					c = ""
				if p:
					js = f"({p}){js}"
				l += f"{hz}{c}[{note}{js}]"
			hzs = l
		elif len(fs) > 3 and fs[3]:
			sy, sd, _, hzs = fs[:4]
			hzs = hzs.replace(")(", "；").replace("）（", "；")
			hzs = re.sub(r"(\d)([-=])", "\\2\\1", hzs)
			hzs = self.normS(hzs)
		else:
			sy, sd, hzs = fs[:3]
		if not yb: yb = sy + sd
		l = list()
		hzs = self.normM(hzs)
		hzs = re.sub(r"(〚.*?〛)([-=])", "\\2\\1", hzs)
		for hz, c, o, js in re.findall(r"(.)([-=*?+]?)([₀-₉0-9]?) *(〚.*?〛)?", hzs):
			if js: js = js[1:-1]
			js = o + js
			l.append((hz, yb + c, js))
		return l