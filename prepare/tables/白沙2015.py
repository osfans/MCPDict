#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "och_ba"
	_file = "BaxterSagartOC2015-10-13.tsv"
	_lang = "上古（白一平沙加爾2015）"
	note = "版本：2015-10-13<br>來源：<a href=http://ocbaxtersagart.lsait.lsa.umich.edu/>上古音白一平沙加爾2015年擬音</a>"
	tones = None

	def parse(self, fs):
		return fs[0], fs[4]
