#!/usr/bin/env python3

from tables._表 import 表 as _表
from collections import defaultdict
import re

class 表(_表):
	full = "漢語大字典"
	short = "漢大"
	note = "來源：<a href=https://github.com/zi-phoenicia/hydzd/>GitHub</a>"
	dictionary = True
	
	def update(self):
		d = defaultdict(list)
		hd = defaultdict(dict)
		numbers="❶❷❸❹❺❻❼❽❾❿⓫⓬⓭⓮⓯⓰⓱⓲⓳⓴㉑㉒㉓㉔㉕㉖㉗㉘㉙㉚㉛㉜㉝㉞㉟㊱㊲㊳㊴㊵㊶㊷㊸㊹㊺㊻㊼㊽㊾㊿"
		for line in open(self.spath,encoding="U8"):
			fs = line.strip('\n').split('\t')
			if len(fs[0]) <= 2:
				hzs,py,js,page = fs[:4]
				hz = hzs[0]
				if hz in self.kCompatibilityVariants and js.startswith("同"): continue
				if page not in hd[hz]:
					hd[hz][page] = dict()
				if py == "None":
					py = ""
				py = py.rstrip("5")
				if len(hzs) > 1:
					py = f"{py} ({hzs})"
				if py in hd[hz][page]:
					hd[hz][page][py].append(js)
				else:
					hd[hz][page][py] = [js]
		for hz in hd:
			for page in hd[hz]:
				for py in hd[hz][page]:
					if len(hd[hz][page][py])!=1:
						hd[hz][page][py] = [numbers[count]+js for count,js in enumerate(hd[hz][page][py])]
		for hz in hd:
			for page in hd[hz]:
				js = "\t\t".join(["%s\t%s" % (py, "\t".join(hd[hz][page][py])) for py in hd[hz][page]])
				js = re.sub("=(.[GTJKUXV]?)", "“\\1”", js).strip()
				if hz not in d:
					d[hz] = []
				d[hz].append("%s\t%s"%(page, js))
		self.write(d)
