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

	def __init__(自):
		super().__init__()
		自.smd = dict()
		自.ymd = dict()
		自.sdd = {
			"5": "9",
			"6": "8",
			"2": "7a",
			"1": "7b",
		}
		ipa = 0
		for 行 in 自.raw.split("\n"):
			行 = 行.strip()
			if not 行: continue
			if "IPA" in 行:
				ipa += 1
				continue
			列 = 行.split("\t")
			n = len(列)
			for i in range(0, n, 2):
				if not 列[i]: continue
				if ipa == 1:
					自.smd[列[i]] = 列[i + 1].strip("/ ")
				elif ipa == 2:
					自.ymd[列[i]] = 列[i + 1].strip("/ ")

	def 析(自, 列):
		字, sm, ym, sd, js = 列[:5]
		sm = sm.strip()
		ym = ym.strip().replace("ɡ","g")
		if not ym:
			ym = ""
		elif ym not in 自.ymd:
			ym = ym[0] + 自.ymd[ym[1:]]
		else:
			ym = 自.ymd[ym]
		if ym and ym[-1] in "ptk":
			sd = 自.sdd.get(sd, sd)
		sm = 自.smd[sm] if sm else ""
		yb = sm + ym + sd
		return 字, yb, js
