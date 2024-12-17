#!/usr/bin/env python3

import re
from tables._表 import 表 as _表

class 表(_表):
	sms = {'g': 'k', 'd': 't', '': '', 'c': 'tsʰ', 'b': 'p', 'l': 'l', 'h': 'x', 't': 'tʰ', 'q': 'tɕʰ', 'z': 'ts', 'j': 'tɕ', 'f': 'f', 'k': 'kʰ', 'n': 'n', 'x': 'ɕ', 'm': 'm', 's': 's', 'p': 'pʰ', 'ng': 'ŋ'}
	yms = {'ae': 'e', 'ieh': 'iəʔ', 'ii': 'iɪ̃', 'eh': 'əʔ', 'io': 'iɔ', 'ieu': 'iɤɯ', 'u': 'ʊ̃', 'v': 'uᵝ', 'en': 'ən', 'a': 'ɑ', 'on': 'ɔŋ', 'an': 'ã', 'oh': 'ɔʔ', 'i': 'iᶽ', 'ien': 'in', 'ion': 'iɔŋ', 'ah': 'aʔ', 'ih': 'iʔ', 'y': 'yᶽ', 'ui': 'uɪ', 'uae': 'ue', 'aeh': 'ɛʔ', 'in': 'iɪ̃', 'ia': 'iɑ', 'z': 'ɿ', 'uh': 'ʊʔ', 'aen': 'ɛ̃', 'eu': 'ɤɯ', 'iah': 'iaʔ', 'ueh': 'uəʔ', 'iae': 'ie', 'iuh': 'yʊʔ', 'yen': 'yn', 'ian': 'iã', 'iun': 'yʊ̃', 'un': 'ʊ̃', 'o': 'ɔ', 'uan': 'uã', 'ua': 'uɑ', 'uen': 'uən', 'ioh': 'iɔʔ', 'iaen': 'iɛ̃', 'uaen': 'uɛ̃', 'uaeh': 'uɛʔ', 'iaeh': 'iɛʔ', 'uah': 'uaʔ', 'yeh': 'yəʔ', 'ya': 'yɑ', '': ''}
	disorder = True

	def parse(self, fs):
		if len(fs) < 2: return
		if fs[0].startswith("#"): return
		pys, hzs = fs
		ybs = []
		for py in pys.split("/"):
			sm = re.findall("^[^aeiouvy]?g?", py)[0]
			sd = py[-1]
			if sd not in "12357": sd = ""
			ym = py[len(sm):len(py)-len(sd)]
			yb = self.sms[sm]+self.yms[ym]+sd
			yb = re.sub("(sʰ?)u([aɑeəɪ])", "\\1ɥ\\2", yb)
			ybs.append(yb)
		if len(ybs) > 1:
			ybs[0] = f"`{ybs[0]}`"
		yb = "/".join(ybs)
		hzs = hzs.replace("<", "{字彙音}")
		hzs = re.findall(r"(.)([+-=*?]?)(\{.*?\})?", hzs)
		l = list()
		for hz, c, js in hzs:
			p = ""
			if js: js = js[1:-1]
			js = p + js
			l.append((hz, yb + c, js))
		return l
