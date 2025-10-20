#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	sets = set()

	def py2yb(自, s):
		s = re.sub("^(.*)-([a-z].*)$", "\\2", s)
		s = re.sub("-.*$", "", s)
		s = s.replace("ae","ɛ").replace("o","ɔ").replace("ei","ɛi").replace("ui","uɛi").replace("ii","iɪ")
		s = s.replace("eu", "ɤɯ").replace("e", "ə").replace("v", "uᵝ")
		s = re.sub("u(\\d)$", "ʊ\\1", s)
		s = re.sub("z(\\d)$", "ɿ\\1", s)
		s = re.sub("^([^aeiouy]?)i(\\d)$", "\\1iᶽ\\2", s)
		s = re.sub("y(\\d)$", "yᶽ\\1", s)
		s = s.replace("p", "pʰ").replace("t", "tʰ").replace("k", "kʰ")
		s = s.replace("b", "p").replace("d", "t").replace("ng", "ŋ").replace("g", "k")
		s = s.replace("z", "ts").replace("c", "tsʰ").replace("s", "s")
		s = s.replace("j", "tɕ").replace("q", "tɕʰ").replace("x", "ɕ").replace("h7", "ʔ7").replace("h", "x")
		s = s.replace("ɛʔ", "æʔ").replace("iʔ", "iɪʔ")
		s = s.replace("an", "aŋ").replace("ɔn", "oŋ").replace("in", "iɪ̃").replace("iən", "in").replace("ɛn", "ɛ̃").replace("iun", "yõ").replace("un", "õ")
		return s.strip()

	def 析(自, 列):
		if len(列) < 2: return
		cy,pys= 列[:2]
		cy2 = re.sub(r'[=\+\-\*0-9]', '', cy)
		pys = pys.split(" ")
		js = 列[2] if len(列) > 2 else ''
		if js:
			if len(cy2) == 1:
				zs = js
			else:
				zs = f"【{cy2}】{js}"
		elif len(cy2) > 1:
			zs = cy2
		else:
			zs = ''
		l = list()
		cy = cy.replace("，", "")
		for i,z in enumerate(re.findall(r'.[=\+\-\*0-9]?', cy)):
			yb = 自.py2yb(pys[i])
			if z[0] + yb in 自.sets:
				continue
			自.sets.add(z[0] + yb)
			mark = ""
			if len(z) > 1 and z[1] in "+-=":
				mark = z[1]
			l.append((z[0], yb + mark, zs))
		return l
