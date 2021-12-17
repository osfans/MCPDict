#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "yue_nnbh"
	_file = "naamning_baakwaa.dict.yaml"
	note = "更新：2021-07-13<br>來源：<a href=https://github.com/leimaau/naamning_jyutping>南寧話輸入方案</a><br>說明：心母字讀 sl[ɬ]（清齒齦邊擦音），效咸山攝二等字讀 -eu[-ɛu]、-em[-ɛm]/-ep[-ɛp]、-en[-ɛn]/-et[-ɛt]<br>老派的師韻（止開三精莊組）字讀 zy[tsɿ]、cy[tsʰɿ]、sy[sɿ]，津韻（臻合三舌齒音、部份臻開三）字讀 -yun[-yn]/-yut[-yt]<br>(白)白讀；(文)文讀；(老派)老派音；(習)習讀；(常)常讀；(又)又讀；(罕)罕讀；(訓)訓讀；(舊)舊讀；(語)口語音；(書)書面音；(外)外來語音譯；(名)名詞；(動)動詞；(量)量詞"
	tones = "55 1 1a 陰平 ꜀,35 3 2a 陰上 ꜂,33 5 3a 陰去 ꜄,21 2 1b 陽平 ꜁,24 4 2b 陽上 ꜃,22 6 3b 陽去 ꜅,55 7a 4a 上陰入 ꜆,33 7b 4b 下陰入 ꜀,22 8 4c 陽入 ꜇"
	xlit="PmfTnNlKhHsʃjwWɐAEɛIɪɔOœɵUʊYː]|pmftnŋlkhʰsʃjwʷɐaeɛiɪɔoœɵuʊyː̚"
	xlits = list(zip(*xlit.split("|")))

	def py2yb(self, py):
		py = re.sub("([ptk])1", "\g<1>7", py)
		py = re.sub("([ptk])3", "\g<1>8", py)
		py = re.sub("([ptk])6", "\g<1>9", py)
		py = re.sub("^(m)+$", "\\1̩", py)
		py = re.sub("^(ng)+$", "\\1̍", py)
		py = re.sub("^q", "ʔ", py)
		py = re.sub("^([jy])u(ng)","jʊŋ", py)
		py = re.sub("^(jy|[jy])u([t])", "jYː\\2]", py)
		py = re.sub("([dtlgkhzcsj])yu([t])", "\\1Yː\\2]", py)
		py = re.sub("sl", "ɬ", py)
		py = re.sub("^([jy])u([k])","jʊ\\2]", py)
		py = re.sub("^(jy)u", "jYː", py)
		py = re.sub("yu", "Yː", py)
		py = re.sub("y([aeior])", "j\\1", py)
		py = re.sub("(aa|r)([i])", "AːI", py)
		py = re.sub("(aa|r)([u])", "AːU", py)
		py = py.replace("ai", "ɐI").replace("au", "ɐU")
		py = re.sub("(aa|r)([ptk])", "Aː\\2]", py)
		py = re.sub("a([ptk])", "ɐ\\1]", py)
		py = re.sub("(aa|r)", "Aː", py)
		py = re.sub("^b", "P", py)
		py = re.sub("c", "T͡ʃH", py)
		py = re.sub("^d", "T", py)
		py = re.sub("eu", "ɛːU", py)
		py = re.sub("(eo|oe)i", "ɵY", py)
		py = re.sub("(eo|oe)([pk])", "œː\\2]", py)
		py = re.sub("(eo|oe)(ng)", "œː\\2", py)
		py = re.sub("(eo|oe)(t)", "ɵ\\2]", py)
		py = re.sub("(eo|oe)(n)", "ɵ\\2", py)
		py = py.replace("oe", "œː").replace("oi", "ɔːI")
		py = re.sub("ou", "OU", py)
		py = re.sub("u([k])", "ʊ\\1]", py)
		py = re.sub("ui", "UːI", py)
		py = re.sub("iu", "IːU", py)
		py = re.sub("i(ng)", "EN", py)
		py = re.sub("ik", "EK]", py)
		py = re.sub("i([pt])", "Iː\\1]", py)
		py = py.replace("eo", "ɵ").replace("a", "ɐ").replace("ei", "EI").replace("i", "Iː")
		py = re.sub("e([ptk])", "ɛː\\1]", py)
		py = re.sub("e", "ɛː", py)
		py = re.sub("o([ptk])", "ɔː\\1]", py)
		py = re.sub("u([pt])", "Uː\\1]", py)
		py = re.sub("u(ng)", "ʊN", py)
		py = re.sub("o", "ɔː", py)
		py = re.sub("u", "Uː", py)
		py = re.sub("em", "ɛːm", py)
		py = re.sub("en", "ɛːn", py)
		py = py.replace("ng", "N").replace("kw", "KWH").replace("gw", "KW").replace("g", "K")
		py = re.sub("^([ptk])", "\\1H", py)
		py = re.sub("s", "ʃ", py)
		py = re.sub("z", "T͡ʃ", py)
		py = re.sub("T͡ʃy", "T͡sɿ", py)
		py = re.sub("T͡ʃHy", "T͡sHɿ", py)
		py = re.sub("ʃy", "sɿ", py)
		py = re.sub("T͡ʃT͡ʃ", "T͡sɿ", py)
		py = re.sub("T͡ʃHT͡ʃ", "T͡sHɿ", py)
		py = re.sub("ʃT͡ʃ", "sɿ", py)
		py = re.sub("T͡ʃIːIː", "T͡sɿ", py)
		py = re.sub("T͡ʃHIːIː", "T͡sHɿ", py)
		py = re.sub("ʃIːIː", "sɿ", py)
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
