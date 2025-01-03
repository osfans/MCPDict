#!/usr/bin/env python3

import re, json
from collections import defaultdict
from tables._表 import 表 as _表

class 表(_表):
	#https://github.com/g0v/moedict-data-hakka/blob/master/dict-hakka.json
	網站 = "客語萌典"
	網址 = "https://www.moedict.tw/:%s"
	def py2yb(自, s):
		c = s[-1]
		if c in "文白":
			s = s[:-1]
		else:
			c = ""
		s = s.replace("er","ə").replace("ae","æ").replace("ii", "ɿ").replace("e", "ɛ").replace("o", "ɔ")
		s = s.replace("sl", "ɬ").replace("nj", "ɲ").replace("t", "tʰ").replace("zh", "tʃ").replace("ch", "tʃʰ").replace("sh", "ʃ").replace("p", "pʰ").replace("k", "kʰ").replace("z", "ts").replace("c", "tsʰ").replace("j", "tɕ").replace("q", "tɕʰ").replace("x", "ɕ").replace("rh", "ʒ").replace("r", "ʒ").replace("ng", "ŋ").replace("?", "ʔ").replace("b", "p").replace("d", "t").replace("g", "k")
		s = 自.轉調類(s)
		if c == "文":
			s+="="
		elif c == "白":
			s += "-"
		return s.strip()

	def 更新(自):
		d = defaultdict(list)
		tk = json.load(open(自.spath,encoding="U8"))
		for 行 in tk:
				字 = 行["title"]
				heteronyms = 行["heteronyms"]
				if len(字) != 1: continue
				for i in heteronyms:
					pys = i["pinyin"]
					py = re.findall(rf"{自.簡稱[2]}\u20DE(.+?)\b", pys)
					if py and py[0]:
						yb = 自.py2yb(py[0]).strip()
						if yb: d[字].append(yb)
		自.寫(d)

