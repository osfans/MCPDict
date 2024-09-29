#!/usr/bin/env python3

import re
from tables._表 import 表 as _表

class 表(_表):
	site = "淮語字典"
	url = "https://huae.sourceforge.io/query.php?table=類音字彙&字=%s"
	sms = {'g': 'k', 'd': 't', '': '', 'c': 'tsʰ', 'b': 'p', 'l': 'l', 'h': 'x', 't': 'tʰ', 'q': 'tɕʰ', 'z': 'ts', 'j': 'tɕ', 'f': 'f', 'k': 'kʰ', 'n': 'n', 'x': 'ɕ', 'm': 'm', 's': 's', 'p': 'pʰ', 'ng': 'ŋ'}
	yms = {'ae': 'ɛ', 'ieh': 'iəʔ', 'ii': 'iɪ̃', 'eh': 'əʔ', 'io': 'iɔ', 'ieu': 'iɤɯ', 'u': 'õ', 'v': 'u', 'en': 'ən', 'a': 'ɑ', 'on': 'ɔŋ', 'an': 'ɑ̃', 'oh': 'ɔʔ', 'i': 'i', 'ien': 'in', 'ion': 'iɔŋ', 'ah': 'aʔ', 'ih': 'iʔ', 'y': 'y', 'ui': 'uɪ', 'uae': 'uɛ', 'aeh': 'æʔ', 'in': 'iɪ̃', 'ia': 'iɑ', 'z': 'ɿ', 'uh': 'oʔ', 'aen': 'ɛ̃', 'eu': 'ɤɯ', 'iah': 'iaʔ', 'ueh': 'uəʔ', 'iae': 'iɛ', 'iuh': 'yoʔ', 'yen': 'yn', 'ian': 'iɑ̃', 'iun': 'yõ', 'un': 'õ', 'o': 'ɔ', 'uan': 'uɑ̃', 'ua': 'uɑ', 'uen': 'uən', 'ioh': 'iɔʔ', 'iaen': 'iɛ̃', 'uaen': 'uɛ̃', 'uaeh': 'uæʔ', 'iaeh': 'iæʔ', 'uah': 'uaʔ', 'yeh': 'yəʔ', 'ya': 'ya', '': ''}
	disorder = True

	def parse(self, fs):
		if len(fs) < 2: return
		if fs[0].startswith("#"): return
		py, hzs = fs
		sm = re.findall("^[^aeiouvy]?g?", py)[0]
		sd = py[-1]
		if sd not in "12357": sd = ""
		ym = py[len(sm):len(py)-len(sd)]
		yb = self.sms[sm]+self.yms[ym]+sd
		hzs = re.findall(r"(.)([+-=*?]?)(\{.*?\})?", hzs)
		l = list()
		for hz, c, js in hzs:
			p = ""
			if js: js = js[1:-1]
			js = p + js
			l.append((hz, yb + c, js))
		return l
