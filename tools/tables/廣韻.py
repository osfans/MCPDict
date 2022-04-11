#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	note = "來源：<a href=https://ytenx.org/kyonh/>韻典網</a><br>說明：括號中注明了《廣韻》中的聲母、韻攝、韻目、等、呼、聲調，以及《平水韻》中的韻部。對於“支脂祭真仙宵侵鹽”八個有重紐的韻，僅在聲母爲脣牙喉音時標註A、B類。廣韻韻目中缺少冬系上聲、臻系上聲、臻系去聲和痕系入聲，“韻典網”上把它們補全了，分別作“湩”、“𧤛”、“櫬”、“麧”。由於“𧤛”字不易顯示，故以同韻目的“齔”字代替。"
	site = '韻典網（廣韻）'
	url = 'http://ytenx.org/zim?kyonh=1&dzih=%s'
	last = ""
	isYb = False
	jointer = "\n"
	
	def __init__(self):
		self.pq = self.getPQ()

	def getPQ(self):
		pq = dict()
		for line in open(self.get_fullname("PrengQim.txt"),encoding="U8"):
			line = line.strip()
			fs = line.split(" ")
			pq[fs[0]] = fs[1].replace("'", "0")
		return pq

	def parse(self, fs):
		hz = fs[0]
		js = fs[3]
		if js == "[同上]": js = "同上"
		if "上同" in js: js = js.replace("上同", "同" + self.last)
		elif "同上" in js: js = js.replace("同上", "同" + self.last)
		else: self.last = hz
		py = self.pq[fs[1]]
		return hz, py, js

	@property
	def syCount(self):
		return len(set(map(lambda x:x.rstrip("hx"), self.syds.keys())))
