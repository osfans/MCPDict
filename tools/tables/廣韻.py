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

	def 分音(自, 音):
		if 音[-1] in "qh":
			調 = 音[-1]
			聲韻 = 音[:-1]
		else:
			調 = "1"
			聲韻 = 音
		return 聲韻,調