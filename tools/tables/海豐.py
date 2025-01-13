#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	toneValues = {'阴平':1,'阳平':2,'阴上':3,'阳上':4,'阴去':5,'阳去':6,'阴入':7,'阳入':8}

	def 析(自, 列):
		l = list()
		字,wds,bds,註 = 列[:4]
		if not 字: return
		字 = 字[0]
		異讀 = len(bds) > 0 and len(wds) > 0
		if 註:
			for i in 自.toneValues:
				註 = 註.replace(i, str(自.toneValues[i]))
		for 音集 in (bds, wds):
			if not 音集: continue
			音 = 音集
			for i in 自.toneValues:
				音 = 音.replace(i, str(自.toneValues[i]))
			if 異讀:
				c = '-' if 音集 == bds else '='
				音 = 音 + c
			if 音.startswith("["):
				註 += 音[:3]
				音 = 音[3:]
			if "训" in 音:
				音 = 音.replace("训", "@")
			l.append((字, 音, 註))
		return l
