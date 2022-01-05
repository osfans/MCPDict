#!/usr/bin/env python3

import re
from tables._表 import 表

class 字表(表):
	key = "cmn_jh_hc_yc"
	note = "更新：2021-09-17<br>來源：osfans整理自<a href=http://huae.nguyoeh.com/>《類音字彙》</a>、《鹽城縣志》"
	site = "淮語字典"
	url = "https://huae.sourceforge.io/query.php?table=類音字彙&字=%s"
	tones = "31 1 1a 陰平 ꜀,213 2 1b 陽平 ꜁,55 3 2 上 ꜂,,35 5 3 去 ꜄,,5 7 4 入 ꜆"
	sms = {'g': 'k', 'd': 't', '': '', 'c': 'tsʰ', 'b': 'p', 'l': 'l', 'h': 'x', 't': 'tʰ', 'q': 'tɕʰ', 'z': 'ts', 'j': 'tɕ', 'f': 'f', 'k': 'kʰ', 'n': 'n', 'x': 'ɕ', 'm': 'm', 's': 's', 'p': 'pʰ', 'ng': 'ŋ'}
	yms = {'ae': 'ɛ', 'ieh': 'iəʔ', 'ii': 'iɪ', 'eh': 'əʔ', 'io': 'iɔ', 'ieu': 'iɤɯ', 'u': 'ʊ', 'v': 'u', 'en': 'ən', 'a': 'a', 'on': 'ɔŋ', 'an': 'ã', 'oh': 'ɔʔ', 'i': 'i', 'ien': 'in', 'ion': 'iɔŋ', 'ah': 'aʔ', 'ih': 'iʔ', 'y': 'y', 'ui': 'uɪ', 'uae': 'uɛ', 'aeh': 'ɛʔ', 'in': 'ĩ', 'ia': 'ia', 'z': 'ɿ', 'uh': 'ʊʔ', 'aen': 'ɛ̃', 'eu': 'ɤɯ', 'iah': 'iaʔ', 'ueh': 'uəʔ', 'iae': 'iɛ', 'iuh': 'yʊʔ', 'yen': 'yn', 'ian': 'iã', 'iun': 'yʊ̃', 'un': 'ʊ̃', 'o': 'ɔ', 'uan': 'uã', 'ua': 'ua', 'uen': 'uən', 'ioh': 'iɔʔ', 'iaen': 'iɛ̃', 'uaen': 'uɛ̃', 'uaeh': 'uɛʔ', 'iaeh': 'iɛʔ', 'uah': 'uaʔ', 'yeh': 'yəʔ', 'ya': 'ya', '': ''}
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
		hzs = re.findall("(.)([+-=*?]?)(\{.*?\})?", hzs)
		l = list()
		for hz, c, js in hzs:
			p = ""
			if js: js = js[1:-1]
			js = p + js
			l.append((hz, yb + c, js))
		return l
