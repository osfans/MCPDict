#!/usr/bin/env python3

import re
from tables._表 import 表

def py2ipa(s):
	tone = s[-1]
	if tone.isdigit():
		s = s[:-1]
	else:
		tone = ""
	s = re.sub("y$", "ɿ", s)
	s = re.sub("^yu$", "ghiu", s)
	s = re.sub("yu$", "ʮ", s)
	s = re.sub("q$", "h", s)
	s = s.replace("y", "ghi").replace("w", "ghu").replace("ii", "i").replace("uu", "u")
	s = re.sub("iu$", "yⱼ", s)
	s = s.replace("au", "æ").replace("ieu", "y").replace("eu", "øʏ").replace("oe", "ø")\
					.replace("an", "ã").replace("aon", "ɑ̃").replace("en", "ən")\
					.replace("iuh", "yəʔ").replace("iu", "y").replace("ou", "əu").replace("aeh", "aʔ").replace("ah", "ɑʔ").replace("ih", "iəʔ").replace("eh", "əʔ").replace("on", "oŋ")
	s = re.sub("h$", "ʔ", s)
	s = re.sub("er$", "əl", s)
	s = s.replace("ph", "pʰ").replace("th", "tʰ").replace("kh", "kʰ").replace("tsh", "tsʰ")\
					.replace("ch", "tɕʰ").replace("c", "tɕ").replace("sh", "ɕ").replace("j", "dʑ").replace("zh", "ʑ")\
					.replace("gh", "ɦ").replace("ng", "ŋ").replace("gn", "ȵ").replace("g", "ɡ")
	s = re.sub("i$", "iⱼ", s)
	s = re.sub("ie$", "i", s)
	s = re.sub("e$", "ᴇ", s)
	s = s + tone
	return s

class 字表(表):
	key = "wuu_th_shj_sz"
	site = "吳語學堂（蘇州）"
	url = "https://www.wugniu.com/search?table=suzhou_zi&char=%s"
	_file = "wugniu_soutseu.dict.yaml"

	def parse(self, fs):
		if len(fs) < 2: return
		hz, py = fs[:2]
		yb = py2ipa(py.strip())
		return hz, yb

