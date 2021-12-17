#!/usr/bin/env python3

import re
from tables._數據庫 import 字表 as 表

def py2ipa(s):
	tag = s[0]
	isTag = tag == "|"
	if isTag:
		s = s[1:-1]
	b = s
	tone = s[-1]
	if tone.isdigit():
		s = s[:-1]
	else:
		tone = ""
	s = re.sub("y$", "ɿ", s)
	s = s.replace("y", "ghi").replace("w", "ghu").replace("ii", "i").replace("uu", "u")
	s = re.sub("^(c|ch|j|sh|zh)u", "\\1iu", s)
	s = s.replace("au", "ɔ").replace("eu", "ɤ").replace("oe", "ø")\
			.replace("an", "ã").replace("aon", "ɑ̃").replace("en", "ən")\
			.replace("iuh", "yiʔ").replace("iu", "y").replace("eh", "əʔ").replace("on", "oŋ")
	s = re.sub("h$", "ʔ", s)
	s = re.sub("r$", "əl", s)
	s = s.replace("ph", "pʰ").replace("th", "tʰ").replace("kh", "kʰ").replace("tsh", "tsʰ")\
			.replace("ch", "tɕʰ").replace("c", "tɕ").replace("sh", "ɕ").replace("j", "dʑ").replace("zh", "ʑ")\
			.replace("gh", "ɦ").replace("ng", "ŋ").replace("g", "ɡ")
	s = re.sub("e$", "ᴇ", s)
	s = s + tone
	if isTag:
		s = "%s-" % s
	return s

class 字表(表):
	key = "wuu_th_shj_sh"
	dbkey = "sh"
	site = "吳音小字典（上海）"
	url = "http://www.wu-chinese.com/minidict/search.php?searchlang=zaonhe&searchkey=%s"
	note = "來源：《上海市區方言志》（1988年版），蔡子文錄入<br>說明：該書記錄的是中派上海話音系（使用者多出生於20世紀40至70年代），與<a href=http://www.wu-chinese.com/minidict/>吳音小字典</a>記錄的音系並不完全相同。"
	tones = "53 1 1 平 ꜀,,,,34 5 3a 陰去 ꜄,23 6 3b 陽去 ꜅,55 7 4a 陰入 ꜆,12 8 4b 陽入 ꜇"
	patches = {"眙": "ɦi6", "汩": "kuəʔ7", "汨": "miʔ8"}

	def format(self, py):
		return py2ipa(py)
