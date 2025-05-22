#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	爲音 = False

	def 析(自, 列):
		字 = 列[1]
		if len(字) > 1 and 字[0] not in '⿰⿱⿲⿳⿴⿵⿶⿷⿸⿹⿺⿻⿼⿽⿾⿿':
			字 = 字[-2]
		音 = '/'.join(列[8:-1] + 列[2:8])
		註 = 列[-1]
		return 字, 音, 註

	@property
	def 聲韻數(自):
		return len(set(map(lambda x:x.rstrip("qh"), 自.音典.keys())))
