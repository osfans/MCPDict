#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "csp_nntz"
	_color = "#FF9900"
	_file = "naamning_bingwaa.dict.yaml"
	note = "更新：2021-07-13<br>來源：<a href=https://github.com/leimaau/naamning_jyutping>南寧話輸入方案</a><br>說明：<br>心母字讀 sl[ɬ]（清齒齦邊擦音），日母和疑母細音字讀 nj[ȵ]（齦齶音）<br>老派的疑母模韻字讀 ngu[ŋu]，微母遇攝臻攝字讀 fu[fu]、fat[fɐt]、fan[fɐn]，遇合一讀o[o]，果合一讀u[u]<br> (白)白讀；(文)文讀；(老派)老派音；(習)習讀；(常)常讀；(又)又讀；(罕)罕讀；(訓)訓讀；(舊)舊讀；(語)口語音；(書)書面音；(外)外來語音譯；(名)名詞；(動)動詞；(量)量詞"
	tones = "53 1 1a 陰平 ꜀,33 3 2a 陰上 ꜂,55 5 3a 陰去 ꜄,21 2 1b 陽平 ꜁,24 4 2b 陽上 ꜃,22 6 3b 陽去 ꜅,55 7a 4a 上陰入 ꜆,33 7b 4b 下陰入 ꜀,24 8a 4c 上陽入 ꜇,22 8b 4d 下陽入 ꜁"
	xlit="PmfTnNlKhHsʃjwWɐAEɛIɪɔOœɵUʊYː]|pmftnŋlkhʰsʃjwʷɐaeɛiɪɔoœɵuʊyː̚"
	xlits = list(zip(*xlit.split("|")))

	def py2yb(self, py):
		py = re.sub("([ptk])3", "\g<1>7", py)
		py = re.sub("([ptk])2", "\g<1>8", py)
		py = re.sub("([ptk])5", "\g<1>9", py)
		py = re.sub("([ptk])6", "\g<1>10", py)
		py = re.sub("^(m)+$", "\\1̩", py)
		py = re.sub("^(ng)+$", "\\1̍", py)
		py = re.sub("^q", "ʔ", py)
		py = re.sub("^(jy|[jy])u([t])", "jYː\\2]", py)
		py = re.sub("([dtlgkhzcsj])yu([t])", "\\1Yː\\2]", py)
		py = re.sub("sl", "ɬ", py)
		py = re.sub("^(jy)u", "jYː", py)
		py = re.sub("yu", "Yː", py)
		py = re.sub("y([aeior])", "j\\1", py)
		py = re.sub("(aa|r)([i])", "AːI", py)
		py = re.sub("(aa|r)([u])", "AːU", py)
		py = re.sub("a([i])", "ɐI", py)
		py = re.sub("a([u])", "ɐU", py)
		py = re.sub("(aa|r)([ptk])", "Aː\\2]", py)
		py = re.sub("a([ptk])", "ɐ\\1]", py)
		py = re.sub("(aa|r)", "Aː", py)
		py = re.sub("^b", "P", py)
		py = re.sub("c", "T͡ʃH", py)
		py = re.sub("^d", "T", py)
		py = re.sub("eu", "ɛːU", py)
		py = re.sub("oe([ptk])", "œː\\1]", py)
		py = re.sub("oe(ng)", "œː\\1", py)
		py = re.sub("oe", "œː", py)
		py = re.sub("eo(ng)", "œːŋ", py)
		py = re.sub("eo([k])", "œː\\1]", py)
		py = re.sub("ou", "OU", py)
		py = re.sub("u([k])", "O\\1]", py)
		py = re.sub("ui", "UːI", py)
		py = re.sub("iu", "IːU", py)
		py = re.sub("i(ng)", "EN", py)
		py = re.sub("ik", "EK]", py)
		py = re.sub("i([pt])", "Iː\\1]", py)
		py = re.sub("a", "ɐ", py)
		py = re.sub("ei", "EI", py)
		py = re.sub("i", "Iː", py)
		py = re.sub("e([ptk])", "ɛː\\1]", py)
		py = re.sub("e", "ɛː", py)
		py = re.sub("Iːɐ", "Iɐ", py)
		py = re.sub("Iːɐk", "Iɐk]", py)
		py = re.sub("Iːɐng", "IɐN", py)
		py = re.sub("o([ptk])", "O\\1]", py)
		py = re.sub("u([pt])", "Uː\\1]", py)
		py = re.sub("u(ng)", "ON", py)
		py = re.sub("o", "O", py)
		py = re.sub("u", "Uː", py)
		py = re.sub("ng", "N", py)
		py = re.sub("kw", "KWH", py)
		py = re.sub("gw", "KW", py)
		py = re.sub("g", "K", py)
		py = re.sub("^([ptk])", "\\1H", py)
		py = re.sub("s", "ʃ", py)
		py = re.sub("z", "T͡ʃ", py)
		py = re.sub("em", "ɛːm", py)
		py = re.sub("en", "ɛːn", py)
		py = re.sub("nj", "ȵ", py)
		for a,b in self.xlits:
			py = py.replace(a, b)
		return py
		
	def parse(self, fs):
		if len(fs) < 2: return
		hz = fs[0]
		c, py, js = re.findall("(\(.*?\))?([a-z0-9]+)(「.*?」)?", fs[1])[0]
		yb = self.py2yb(py)
		js = js.strip('「」')
		if c == "(白)":
			yb += "-"
		elif c == "(文)":
			yb += "="
		elif c == "(又)":
			yb += "+"
		elif c == "(訓)":
			yb += "~"
		elif c == "(語)":
			yb += "\\"
		elif c == "(書)":
			yb += "/"
		else:
			js = c + js
		return hz, yb, js
