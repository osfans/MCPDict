#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "cmn_jh_tt_tr"
	_file = "泰如.csv"
	_lang = "泰如方言"
	note = "來源：<a href=http://taerv.nguyoeh.com/>泰如小字典</a>"
	tones = "21 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,213 3 2 上 ꜂,,44 5 3a 陰去 ꜄,22 6 3b 陽去 ꜅,4 7 4a 陰入 ꜆,35 8 4b 陽入 ꜇"
	site = "泰如小字典"
	url = "http://taerv.nguyoeh.com/query.php?table=泰如字典&簡體=%s"
	sms = {'g': 'k', 'd': 't', '': '', 'sh': 'ʂ', 'c': 'tsʰ', 'b': 'p', 'l': 'l', 'h': 'x', 'r': 'ɻ', 'zh': 'tʂ', 't': 'tʰ', 'v': 'ʋ', 'ng': 'ŋ', 'q': 'tɕʰ', 'z': 'ts', 'j': 'tɕ', 'f': 'f', 'ch': 'tʂʰ', 'k': 'kʰ', 'n': 'n', 'x': 'ɕ', 'm': 'm', 's': 's', 'p': 'pʰ'}
	yms = {'ae': 'ɛ', 'ieh': 'iəʔ', 'ii': 'iɪ', 'r': 'ʅ', 'eh': 'əʔ', 'io': 'iɔ', 'ieu': 'iəʊ', 'u': 'ʊ', 'v': 'ʋ', 'en': 'əŋ', 'a': 'a', 'on': 'ɔŋ', 'ei': 'əi', 'an': 'aŋ', 'oh': 'ɔʔ', 'i': 'i', 'ien': 'iŋ', 'ion': 'iɔŋ', 'ah': 'aʔ', 'ih': 'ɪʔ', 'y': 'y', 'uei': 'uəi', 'uae': 'uɛ', 'aeh': 'ɛʔ', 'in': 'ɪ̃', 'ia': 'ia', 'z': 'ɿ', 'uh': 'ʊʔ', 'aen': 'ɛ̃', 'er': 'ɚ', 'eu': 'əʊ', 'iah': 'iaʔ', 'ueh': 'uəʔ', 'iae': 'iɛ', 'iuh': 'iʊʔ', 'yen': 'yəŋ', 'ian': 'iaŋ', 'iun': 'iʊ̃', 'un': 'ʊ̃', 'o': 'ɔ', 'uan': 'uaŋ', 'ua': 'ua', 'uen': 'uəŋ', 'ioh': 'iɔʔ', 'iaen': 'iɛ̃', 'uaen': 'uɛ̃', 'uaeh': 'uɛʔ', 'iaeh': 'iɛʔ', 'uah': 'uaʔ', 'yeh': 'yəʔ', 'ya': 'ya'}

	def parse(self, fs):
		hz = fs[1]
		c = fs[6]
		if fs[3] == fs[4] == 'v': fs[3] = ''
		yb = self.sms[fs[3]]+self.yms[fs[4]]+fs[5]
		js = ""
		if '白' in c or '口' in c or '常' in c or '古' in c or '舊' in c or '未' in c:
			c = '-'
		elif '正' in c or '本' in c:
			js = "本音"
			c = ""
		elif '異' in c or '訓' in c or '避' in c or '又' in c:
			c = "+"
		elif '文' in c or '新' in c or '齶化' in c:
			c = "="
		else:
			c = ""
		yb = yb + c
		return hz, yb, js
