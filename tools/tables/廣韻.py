#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	網站 = '韻典網（廣韻）'
	網址 = 'http://ytenx.org/zim?kyonh=1&dzih=%s'
	爲音 = False

	def 析(自, 列):
		if 列[0] not in ('1919', '3177'):
			列[7] += '切'
		字 = 列[1]
		音 = '/'.join(列[8:-1] + 列[2:8])
		註 = 列[-1]
		return 字, 音, 註

	@property
	def 聲韻調數(自):
		return len(set(map(lambda x:x.split("/")[0], 自.音典.keys())))

	@property
	def 聲韻數(自):
		return len(set(map(lambda x:x.split("/")[0].rstrip("qh"), 自.音典.keys())))
