#!/usr/bin/env python3

from tables._表 import 表

sms = {'p':'p', 'ph':'pʰ', 'b':'b', 'm':'m', 'pf':'pf', 'pfh':'pfʰ', 'bv':'bv', 't':'t', 'th':'tʰ', 'l':'l', 'n':'n', 'ts':'ts', 'tsh':'tsʰ', 'dz':'dz', 's':'s', 'k':'k', 'kh':'kʰ', 'g':'g', 'ng':'ŋ', 'h':'h', '':''}
yms = {'i':'i', 'u':'u', 'a':'a', 'ia':'ia', 'ua':'ua', 'o':'o', 'io':'io', 'e':'e', 'ue':'ue', 'ui':'ui', 'ai':'ai', 'uai':'uai', 'oi':'oi', 'iu':'iu', 'au':'au', 'iau':'iau', 'ou':'ou', 'im':'im', 'am':'am', 'iam':'iam', 'uam':'uam', 'om':'om', 'ing':'iŋ', 'ung':'uŋ', 'ang':'aŋ', 'iang':'iaŋ', 'uang':'uaŋ', 'ong':'oŋ', 'iong':'ioŋ', 'eng':'eŋ', 'ueng':'ueŋ', 'ip':'ip', 'ap':'ap', 'iap':'iap', 'uap':'uap', 'op':'op', 'ik':'ik', 'uk':'uk', 'ak':'ak', 'iak':'iak', 'uak':'uak', 'ok':'ok', 'iok':'iok', 'ek':'ek', 'uek':'uek', 'inn':'ĩ', 'ann':'ã', 'iann':'iã', 'uann':'uã', 'onn':'õ', 'ionn':'iõ', 'enn':'ẽ', 'uenn':'uẽ', 'uinn':'uĩ', 'ainn':'aĩ', 'uainn':'uaĩ', 'oinn':'oĩ', 'iunn':'iũ', 'aunn':'aũ', 'iaunn':'iaũ', 'ounn':'oũ', 'ih':'iʔ', 'uh':'uʔ', 'ah':'aʔ', 'iah':'iaʔ', 'uah':'uaʔ', 'oh':'oʔ', 'ioh':'ioʔ', 'eh':'eʔ', 'ueh':'ueʔ', 'oih':'oiʔ', 'iuh':'iuʔ', 'auh':'auʔ', 'iauh':'iauʔ', 'annh':'ãʔ', 'uannh':'uãʔ', 'iaunnh':'iaũʔ', 'ɿ':'ɿ', 'm':'m', 'ng':'ŋ', 'ək':'ək', 'aannp':'ãːp', 'annp':'ãp', 'uei':'uei', '':'', 'əŋ': 'əŋ'}

class 字表(表):
	key = "nan_cs_cy"
	note = "版本：V1.0 (2021-12-05)<br>來源：<u>與子偕老</u>"
	tones = "21 1 1a 陰平 ꜀,55 3 2a 陰上 ꜂,51 5 3a 陰去 ꜄,32 7 4a 陰入 ꜆,33 2 1b 陽平 ꜁,51 4 2b 陽上 ꜃,331 6 3b 陽去 ꜅,5 8 4b 陽入 ꜇"
	_file = "潮阳话字表1.0.tsv"

	def parse(self, fs):
		_,hz,js,yb,sm,ym,sd = fs[:7]
		ym = ym.rstrip("12345678")
		sd = sd.strip()
		yb = sms[sm] + yms[ym] + sd
		p = "%s\t%s" % (yb, js)
		return hz, yb, js
