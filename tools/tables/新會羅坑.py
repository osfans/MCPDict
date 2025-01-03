#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	raw = """新會羅坑下沙音系									
聲母									
b	p	m	f						
p	pʰ	m	f						
d	t	n	l						
t	tʰ	n	l						
g	k	ng	j	h					
k	kʰ	ŋ	j	h					
z	c	s	v						
ʦ	ʦʰ	s	ʋ						
韻母									
aa	aai	aau	aan	aam	aang	aat	aap	aak	
aa	ai	au	an	am	aŋ	at	ap	ak	
ie		iaau		iaam	iaang		iaap	iaak	
iɛ		iau		iam	iaŋ		iap	iak	
			uaan		uaang				
			uan		uaŋ				
e	ei	eu	en	em	eng	et	ep		
ɛ	ei	ɜu	ɜn	ɜm	əŋ	ɜt	ɜp		
i		iu	in	im	ing	it	ip	ik	
i		iu	in	im	ɪŋ	it	ip	ɪk	
u	ui		un		ung	ut		uk	
u	ui		un		ʊŋ	ut		ʊk	
uo	uoi	ou	uon		ong	uot		ok	
uɔ	uɔi	ou	uɔn		ɔŋ	uɔt		ɔk	
o									
ɔ									
m	ng								
m	ŋ								
聲調									
1	4	2	5	3	6	2	1	6	5
陰平	陽平	陰上	陽上	陰去	陽去	上入	中入	下入	變入
23	22	45	21	23	32	45	23	32	21
"""

	def __init__(自):
		super().__init__()
		自.smd = dict()
		自.ymd = dict()
		自.sdd = {
			"2": "7",
			"1": "8",
			"6": "9",
			"5": "10",
		}
		ipa = 0
		lines = []
		count = 0
		for 行 in 自.raw.split("\n"):
			行 = 行.strip()
			if not 行: continue
			if "聲母" in 行:
				ipa += 1
				count = 0
				continue
			if "韻母" in 行:
				ipa += 1
				count = 0
				continue
			if "聲調" in 行:
				break
			lines.append(行)
			if count % 2 == 1:
				列 = 行.split("\t")
				n = len(列)
				zms = lines[-2].split("\t")
				for i in range(0, n):
					if not 列[i]: continue
					if ipa == 1:
						自.smd[zms[i]]=列[i]
					elif ipa == 2:
						自.ymd[zms[i]]=列[i]
			count += 1

	def 析(自, 列):
		字, sm, ym, sd, js = 列[0], 列[8], 列[9], 列[10], 列[11]
		if sm + ym == "": return
		ym = 自.ymd[ym] if ym else ""
		sm = 自.smd[sm] if sm else ""
		yb = sm + ym + (自.sdd.get(sd, sd) if ym and ym[-1] in "ptk" else sd)
		return 字, yb, js
