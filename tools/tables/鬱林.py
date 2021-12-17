#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "yue_yl"
	_file = "鬱林話字表-粵拼版-21年9月15日.tsv"
	note = "更新：2021-09-15<br>來源：<u>赤鬚夜蜂虎</u>"
	tones = "54 1 1a 陰平 ꜀,33 3 2a 陰上 ꜂,52 5 3a 陰去 ꜄,32 2 1b 陽平 ꜁,13 4 2b 陽上 ꜃,21 6 3b 陽去 ꜅,5 7a 4a 上陰入 ꜆,3 7b 4b 下陰入 ꜀,2 8a 4c 上陽入 ꜇,1 8b 4d 下陽入 ꜁,44 0 0 上陰小 0,45 0 0 下陰小 0,24 0 0 陽小 0"
	ipas={'b∅': 'p', 'p∅': 'pʰ', 'bb∅': 'ɓ', 'm∅': 'm', 'f∅': 'f', 'd∅': 't', 't∅': 'tʰ', 'dd∅': 'ɗ', 'n∅': 'n', 'l∅': 'l', 'sl∅': 'ɬ', 'g∅': 'k', 'k∅': 'kʰ', 'gw∅': 'kʷ', 'kw∅': 'kʷʰ', 'h∅': 'h', 'ng∅': 'ŋ', 'z∅': 'tʃ', 'c∅': 'tʃʰ', 's∅': 'ʃ', 'nj∅': 'ȵ', 'j∅': 'j', 'w∅': 'w', '∅': '', 'aa': 'a', 'ai': 'ai', 'au': 'au', 'an': 'an', 'ah': 'aʔ', 'am': 'am', 'ang': 'aŋ', 'at': 'at', 'ap': 'ap', 'ak': 'ak', 'o': 'ɔ', 'oi': 'ɔi', 'ou': 'ɔu', 'on': 'ɔn', 'om': 'ɔm', 'ong': 'ɔŋ', 'ot': 'ɔt', 'op': 'ɔp', 'ok': 'ɔk', 'oe': 'œ', 'oen': 'œn', 'yng': 'œŋ', 'yet': 'œt', 'yk': 'œk', 'oek': 'œk', 'oep': 'œp', 'e': 'ɛ', 'een': 'ɛn', 'ing': 'eŋ', 'ik': 'ek', 'eo': 'o', 'eou': 'əu', 'eat': 'ət', 'eu': 'ɛu', 'ei': 'ei', 'en': 'ɛn', 'em': 'ɛm', 'eng': 'ɛŋ', 'et': 'ɛt', 'ep': 'ɛp', 'ek': 'ɛk', 'i': 'i', 'iu': 'iu', 'in': 'in', 'im': 'im', 'it': 'it', 'ip': 'ip', 'iik': 'ik', 'u': 'u', 'ui': 'ui', 'un': 'un', 'ung': 'oŋ', 'ut': 'ut', 'uk': 'ok', 'yu': 'y', 'yun': 'yn', 'yut': 'yt', 'm': 'm̩', 'ng': 'ŋ̍', '': ''}
	
	def parse(self, fs):
		if len(fs) < 12: return
		hz = fs[0]
		sm,ym,sd,js = fs[8:12]
		sm = sm.replace("0", "") + "∅"
		yb = (self.ipas[sm] if sm in self.ipas else sm.rstrip("∅"))+self.ipas[ym] + sd
		return hz, yb, js
