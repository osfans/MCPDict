#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	site = '韻典網（廣韻）'
	url = 'http://ytenx.org/zim?kyonh=1&dzih=%s'
	isYb = False

	def parse(self, fs):
		if fs[0] not in ('1919', '3177'):
			fs[7] += '切'
		hz = fs[1]
		yb = '/'.join(fs[8:-1] + fs[2:8])
		js = fs[-1]
		return hz, yb, js

	@property
	def sydCount(self):
		return len(set(map(lambda x:x.split("/")[0], self.syds.keys())))

	@property
	def syCount(self):
		return len(set(map(lambda x:x.split("/")[0].rstrip("qh"), self.syds.keys())))
