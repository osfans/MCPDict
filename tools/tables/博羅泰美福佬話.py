#!/usr/bin/env python3

import re
from tables._表 import 表

class 字表(表):
	key = "nan_zq_bltm"
	_file = "闽南语博罗泰美福佬话字表*.tsv"
	note = "版本：2.3 (2021-12-31)<br>來源：<u>林</u>"
	tones = "55 1 1a 陰平 ꜀,52 3 2 上 ꜂,31 5 3a 陰去 ꜄,2 7 4a 陰入 ꜆,24 2 1b 陽平 ꜁,,33 6 3b 陽去 ꜅,5 8 4b 陽入 ꜇"
	toneValues = {'55':1,'52':2,'31':3,'2':4,'24':5,'33':7,'5':8}

	def parse(self, fs):
		l = list()
		hz,bds,wds,js = fs[:4]
		if not hz: return
		hz = hz[0]
		yd = len(bds) > 0 and len(wds) > 0
		for ybs in (bds, wds):
			if not ybs: continue
			for yb in ybs.split("，"):
				if "（" in yb:
					ybzs = re.findall("^(.*?)（(.*?)）$", yb)
					yb = ybzs[0][0]
					c = ybzs[0][1]
				ipa = yb.rstrip("12345")
				sd = yb[len(ipa):]
				if sd:
					sd = str(self.toneValues[sd])
				yb = ipa + sd
				if yd:
					c = '-' if ybs == bds else '='
					yb = yb + c
				l.append((hz, yb, js))
		return l
