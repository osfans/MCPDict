#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "ltc_zy"
	_lang = "中原音韻"
	#https://github.com/BYVoid/ytenx/blob/master/ytenx/sync/trngyan
	_file = "TriungNgyanQimYonh.txt"
	note = "來源：<a href=https://ytenx.org/trngyan/>韻典網</a><br>說明：平聲分陰陽，入聲派三聲。<b>黑體字</b>表示古入聲字"
	tones = "33 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,214 3 2 上 ꜂,51 5 3 去 ꜄"
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
