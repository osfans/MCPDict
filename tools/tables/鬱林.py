#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	raw = """聲	p	b
聲	pʰ	p
聲	ɓ	bb
聲	m	m
聲	f	f
聲	t	d
聲	tʰ	t
聲	ɗ	dd
聲	n	n
聲	l	l
聲	ɬ	sl
聲	k	g
聲	kʰ	k
聲	kʷ	gw
聲	kʷʰ	kw
聲	h	h
聲	ŋ	ng
聲	tɕ	z
聲	tɕʰ	c
聲	ɕ	s
聲	ȵ	nj
聲	j	j
聲	w	w
聲	∅	0
聲	ɦ	rh
韻	a	a
韻	ai	ai
韻	au	au
韻	an	an
韻	am	am
韻	aŋ	ang
韻	at	at
韻	ap	ap
韻	ak	ak
韻	ɔ	o
韻	ɔɑ̯i	oai
韻	ɒu	ou
韻	ɔɑ̯n	oan
韻	ɒm	om
韻	oːŋ	oong
韻	ɔɑ̯t	oat
韻	ɒp	op
韻	oːk	ook
韻	œ	oe
韻	œa̯m	oem
韻	œa̯n	oen
韻	øŋ	eong
韻	œa̯p	oep
韻	œa̯t	oet
韻	øk	eok
韻	œa̯k	oek
韻	ɛ	e
韻	ei	ei
韻	ɛn	en
韻	eŋ	eng
韻	ᴇɐ̯t	et
韻	ᴇɐ̯u	eu
韻	ek	ek
韻	ɵ	eo
韻	ɘu	eou
韻	om	oom
韻	op	oop
韻	əʔ	eot
韻	ɛa̯u	eau
韻	ɛa̯n	ean
韻	ɛa̯m	eam
韻	ɛa̯ŋ	eang
韻	ɛa̯t	eat
韻	ɛa̯p	eap
韻	ɛa̯k	eak
韻	i	i
韻	iu	iu
韻	in	in
韻	im	im
韻	it	it
韻	ip	ip
韻	u	u
韻	ui	ui
韻	un	un
韻	oŋ	ong
韻	ut	ut
韻	ok	ok
韻	y	yu
韻	ÿn	yun
韻	ÿt	yut
韻	m̩	m
韻	ŋ̍	ng"""

	def __init__(自):
		super().__init__()
		自.smd = dict()
		自.ymd = dict()
		for 行 in 自.raw.split("\n"):
			列 = 行.split("\t")
			if 列[0] == "聲": 自.smd[列[2]] = 列[1]
			elif 列[0] == "韻": 自.ymd[列[2]] = 列[1]
	
	def 析(自, 列):
		if len(列) < 12: return
		字 = 列[0]
		if len(字) != 1: return
		sm,ym,sd,js = 列[8:12]
		sd = sd.lstrip("0")
		yb = 自.smd.get(sm, sm)+(自.ymd[ym] if ym else "") + sd
		return 字, yb, js
