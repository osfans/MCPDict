#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "yue_gl_yl"
	_file = "鬱林話字表-粵拼版-21年9月15日.tsv"
	ipas={'b#': 'p', 'p#': 'pʰ', 'bb#': 'ɓ', 'm#': 'm', 'f#': 'f', 'd#': 't', 't#': 'tʰ', 'dd#': 'ɗ', 'n#': 'n', 'l#': 'l', 'sl#': 'ɬ', 'g#': 'k', 'k#': 'kʰ', 'gw#': 'kʷ', 'kw#': 'kʷʰ', 'h#': 'h', 'ng#': 'ŋ', 'z#': 'tʃ', 'c#': 'tʃʰ', 's#': 'ʃ', 'nj#': 'ȵ', 'j#': 'j', 'w#': 'w', '#': '', 'aa': 'a', 'ai': 'ai', 'au': 'au', 'an': 'an', 'ah': 'aʔ', 'am': 'am', 'ang': 'aŋ', 'at': 'at', 'ap': 'ap', 'ak': 'ak', 'o': 'ɔ', 'oi': 'ɔi', 'ou': 'ɔu', 'on': 'ɔn', 'om': 'ɔm', 'ong': 'ɔŋ', 'ot': 'ɔt', 'op': 'ɔp', 'ok': 'ɔk', 'oe': 'œ', 'oen': 'œn', 'yng': 'œŋ', 'yet': 'œt', 'yk': 'œk', 'oek': 'œk', 'oep': 'œp', 'e': 'ɛ', 'een': 'ɛn', 'ing': 'eŋ', 'ik': 'ek', 'eo': 'o', 'eou': 'əu', 'eat': 'ət', 'eu': 'ɛu', 'ei': 'ei', 'en': 'ɛn', 'em': 'ɛm', 'eng': 'ɛŋ', 'et': 'ɛt', 'ep': 'ɛp', 'ek': 'ɛk', 'i': 'i', 'iu': 'iu', 'in': 'in', 'im': 'im', 'it': 'it', 'ip': 'ip', 'iik': 'ik', 'u': 'u', 'ui': 'ui', 'un': 'un', 'ung': 'oŋ', 'ut': 'ut', 'uk': 'ok', 'yu': 'y', 'yun': 'yn', 'yut': 'yt', 'm': 'm̩', 'ng': 'ŋ̍', '': ''}
	
	def parse(self, fs):
		if len(fs) < 12: return
		hz = fs[0]
		sm,ym,sd,js = fs[8:12]
		sm += "#"
		yb = (self.ipas[sm] if sm in self.ipas else sm.rstrip("#"))+self.ipas[ym] + sd
		return hz, yb, js
