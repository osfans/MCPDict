#!/usr/bin/env python3

import re
from tables._數據庫 import 字表 as 表

class 字表(表):
	key = "nan_zq_tw"
	_lang = "臺灣閩南語"
	dbkey = "mn"
	site = "臺灣閩南語常用詞辭典"
	url = "http://twblg.dict.edu.tw/holodict_new/result.jsp?querytarget=1&radiobutton=0&limit=20&sample=%s"
	note = "更新：2020-05-17<br>來源：<a href=https://github.com/tauhu-tw/tauhu-taigi>豆腐台語詞庫</a>、<a href=https://twblg.dict.edu.tw/holodict_new/>臺灣閩南語常用詞辭典</a><br>說明：下標“俗”表示“俗讀音”，“替”表示“替代字”，指的是某個字的讀音其實來自另一個字，比如“人”字的lang5音其實來自“儂”字。有些字會有用斜線分隔的兩個讀音（如“人”字的jin5/lin5），前者爲高雄音（第一優勢腔），後者爲臺北音（第二優勢腔）。"
	tones = "55 1 1a 陰平 ꜀,51 3 2 上 ꜂,31 5 3a 陰去 ꜄,3 7 4a 陰入 ꜆,24 2 1b 陽平 ꜁,,33 6 3b 陽去 ꜅,5 8 4b 陽入 ꜇"
	patches = {"檔": "tong2,tong3"}

	def format(self, py):
		py = re.sub("\|(.*?)\|", "\\1-", py)
		py = re.sub("\*(.*?)\*", "\\1=", py)
		py = re.sub("\((.*?)\)", "\\1*", py)
		py = re.sub("\[(.*?)\]", "\\1≈", py)
		return py
	
	def patch(self, d):
		for line in open(self.get_fullname("豆腐台語詞庫.csv")):
			fs = line.strip().split(',')
			hz = fs[0]
			if len(hz) == 1:
				for py in fs[1:]:
					if py not in d[hz]:
						d[hz].append(py)
		表.patch(self, d)
