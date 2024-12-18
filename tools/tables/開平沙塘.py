#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	raw = """Jpp	IPA	Jpp	IPA	Jpp	IPA	Jpp	IPA										
b	/p/	p	/pʰ/ 	v	/ʋ/	m	/m/										
																	
f	/f/			s	/s/												
																	
g	/k/	k	/kʰ/	h	/h/	ng	/ŋ/										
																	
ø	/ʔ/																
																	
d	/t/			l	/l/	n	/n/										
																	
z	/ts/	c	/tsʰ/	j	/j/												
																	
Jpp	IPA	Jpp	IPA	Jpp	IPA	Jpp	IPA	Jpp	IPA	Jpp	IPA	Jpp	IPA	Jpp	IPA	Jpp	IPA
aa	/a/	aai	/ai/	aau	/au/	aang	/aŋ/	aak	/ak/	aan	/an/	aat	/at/	aam	/am/	aap	/ap/
																	
o	/ɔ/	oi	/ɔi/			ong	/ɔŋ/	ok	/ɔk/	on	/ɔn/	ot	/ɔt/				
																	
ea	/ə/			eau	/əu/	eang	/əŋ/	eak	/ək/	ean	/ən/	eat	/ət/	eam	/əm/	eap	/əp/
																	
u	/u/	ui	/ui/			ung	/uŋ/	uk	/uk/	un	/un/	ut	/ut/				
																	
i	/i/			iu	/iu/					in	/in/	it	/it/	im	/im/	ip	/ip/
																	
e	/ɛ/	ei	/ei/														
"""

	def __init__(self):
		super().__init__()
		self.smd = dict()
		self.ymd = dict()
		self.sdd = {
			"5": "9",
			"6": "8",
			"2": "7a",
			"1": "7b",
		}
		ipa = 0
		for line in self.raw.split("\n"):
			line = line.strip()
			if not line: continue
			if "IPA" in line:
				ipa += 1
				continue
			fs = line.split("\t")
			n = len(fs)
			for i in range(0, n, 2):
				if not fs[i]: continue
				if ipa == 1:
					self.smd[fs[i]] = fs[i + 1].strip("/ ")
				elif ipa == 2:
					self.ymd[fs[i]] = fs[i + 1].strip("/ ")

	def parse(self, fs):
		hz, sm, ym, sd, js = fs[:5]
		sm = sm.strip()
		ym = ym.strip().replace("ɡ","g")
		if not ym:
			ym = ""
		elif ym not in self.ymd:
			ym = ym[0] + self.ymd[ym[1:]]
		else:
			ym = self.ymd[ym]
		if ym and ym[-1] in "ptk":
			sd = self.sdd.get(sd, sd)
		sm = self.smd[sm] if sm else ""
		yb = sm + ym + sd
		return hz, yb, js
