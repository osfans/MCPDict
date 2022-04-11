#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	#https://github.com/BYVoid/ytenx/blob/master/ytenx/sync/trngyan
	site = '韻典網（中原音韻）'
	url = 'https://ytenx.org/trngyan/dzih/%s'
	
	def __init__(self):
		self.sms = self.getIPA("CjengMuxNgixQim.txt")
		self.yms = self.getIPA("YonhMuxNgixQim.txt")
		self.sds = {'去': '4', '入平': '2', '入去': '4', '入上': '3', '上': '3','陽平': '2', '陰平': '1'}

	def getIPA(self, name):
		yms = dict()
		for line in open(self.get_fullname(name),encoding="U8"):
			line = line.strip('\n')
			if line.startswith('#'): continue
			fs = line.split(' ')
			ym, ipa = fs
			yms[ym] = ipa
		return yms

	def parse(self, fs):
		hzs = fs[1]
		sd = fs[2]
		py = self.sms[fs[4]]+self.yms[fs[5]]+self.sds[sd]
		py = re.sub("([ʂɽ].*?)ɿ", "\\1ʅ", py)
		if sd.startswith("入"):
			py = f"*{py}*"
		l = list()
		for hz in hzs:
			l.append((hz, py))
		return l
