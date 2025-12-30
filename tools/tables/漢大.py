#!/usr/bin/env python3

from tables._表 import 表 as _表
from collections import defaultdict
import re

class 表(_表):
	全稱 = "漢語大字典"
	說明 = "來源：<a href=https://github.com/zi-phoenicia/hydzd/>GitHub</a>"
	字書 = True
	
	def 更新(自):
		d = defaultdict(list)
		hd = defaultdict(dict)
		numbers="❶❷❸❹❺❻❼❽❾❿⓫⓬⓭⓮⓯⓰⓱⓲⓳⓴㉑㉒㉓㉔㉕㉖㉗㉘㉙㉚㉛㉜㉝㉞㉟㊱㊲㊳㊴㊵㊶㊷㊸㊹㊺㊻㊼㊽㊾㊿"
		for 行 in open(自.spath,encoding="U8"):
			列 = 行.strip('\n').split('\t')
			if len(列[0]) <= 2:
				字組,py,js,page = 列[:4]
				字 = 字組[0]
				if 字 in 自.kCompatibilityVariants and js.startswith("同"): continue
				if page not in hd[字]:
					hd[字][page] = dict()
				if py == "None":
					py = ""
				py = py.rstrip("5")
				if len(字組) > 1:
					py = f"{py} ({字組})"
				if py in hd[字][page]:
					hd[字][page][py].append(js)
				else:
					hd[字][page][py] = [js]
		for 字 in hd:
			for page in hd[字]:
				for py in hd[字][page]:
					if len(hd[字][page][py])!=1:
						hd[字][page][py] = [numbers[count]+js for count,js in enumerate(hd[字][page][py])]
		for 字 in hd:
			for page in hd[字]:
				js = "\t\t".join(["%s\t%s" % (py, "\t".join(hd[字][page][py])) for py in hd[字][page]])
				js = js.replace("“=", "“")
				js = re.sub("=(.[GTJKUXV]?)", "“\\1”", js).strip()
				if 字 not in d:
					d[字] = []
				d[字].append("%s\t%s"%(page, js))
		自.寫(d)
