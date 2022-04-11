#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

toneNames = {"陰平": 1, "陽平": 2, "陰上": 3, "陽上": 4, "陰去": 5, "陽去": 6, "陰入": 7, "陽入": 8,
"阴平": 1, "阳平": 2, "阴上": 3, "阳上": 4, "阴去": 5, "阳去": 6, "阴入": 7, "阳入": 8, "":"",
"上聲":3,"去聲":5,"上声":3,"去声":5, "去":5,"上阴入":"7a",'入声':7, '入聲':7,
'第九调':9, '下阴入':'7b', '入':7, '扬入':8, '上陰入':"7a", '陽平乙':'2b', '阳平乙':'2b',
'阳平甲':'2a', '上声上声':3, '第九調':9, '上':3, '陽平甲':'2a', '下陰入':'7b',
'上阳入':'8a', '陰上去':5,'上陽入':'8a', '阴上去':5,'下阳入':'8b','下陽入':'8b', '中入':'7c'}
class 表(_表):
	toned = set()
	def parse(self, fs):
		if len(fs) > 9:
			hz,_,_,_,_,sms,yms,_,dls,js = fs[:10]
		else:
			hz,_,_,_,sms,yms,_,dls,js = fs[:9]
		if dls == "调类": return
		dls = dls.rstrip("12345.").replace("_x0008_","")
		if len(dls) == 4: dls = re.sub("^(..)","\\1/", dls)
		l = list()
		for sm in sms.split("/"):
			for ym in yms.split("/"):
				for dl in dls.split("/"):
					yb = sm + ym + str(toneNames.get(dl, "9"))
					l.append((hz, yb, js))
					js = ""
					if dl not in toneNames: self.toned.add(dl)
		return l
	def write(self, d):
		_表.write(self, d)
		#print(self.toned)
