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
韻	a	aa
韻	ɐi	ai
韻	ɐu	au
韻	ɐn	an
韻	ɐm	am
韻	ɐŋ	ang
韻	ɐt	at
韻	ɐp	ap
韻	ɐk	ak
韻	ɔ	o
韻	ɔi	oi
韻	ɔu	ou
韻	ɔn	on
韻	ɔm	om
韻	ɔŋ	ong
韻	ɔt	ot
韻	ɔp	op
韻	ɔk	ok
韻	œ	oe
韻	øɐ̯m	oem
韻	øɐ̯n	oen
韻	øŋ	yng
韻	øɐ̯p	oep
韻	øɐ̯t	oet
韻	øk	yk
韻	œɐ̯k	oek
韻	ɛ	e
韻	ei	ei
韻	ɛn	een
韻	eŋ	ing
韻	ek	ik
韻	ɵ	eo
韻	ɵ̜u	eou
韻	om	eom
韻	op	eop
韻	ət	eot
韻	ᴇɐ̯u	eu
韻	ᴇɐ̯n	en
韻	ᴇɐ̯m	em
韻	ᴇɐ̯ŋ	eng
韻	ᴇɐ̯t	et
韻	ᴇɐ̯p	ep
韻	ᴇɐ̯k	ek
韻	i	i
韻	iu	iu
韻	in	in
韻	im	im
韻	it	it
韻	ip	ip
韻	ik	iik
韻	u	u
韻	ui	ui
韻	un	un
韻	oŋ	ung
韻	ut	ut
韻	ok	uk
韻	y	yu
韻	ÿn	yun
韻	ÿt	yut
韻	m̩	m
韻	ŋ̍	ng"""

	def __init__(self):
		super().__init__()
		self.smd = dict()
		self.ymd = dict()
		for line in self.raw.split("\n"):
			fs = line.split("\t")
			if fs[0] == "聲": self.smd[fs[2]] = fs[1]
			elif fs[0] == "韻": self.ymd[fs[2]] = fs[1]
	
	def parse(self, fs):
		if len(fs) < 12: return
		hz = fs[0]
		if len(hz) != 1: return
		sm,ym,sd,js = fs[8:12]
		yb = self.smd.get(sm, sm)+(self.ymd[ym] if ym else "") + sd
		return hz, yb, js
