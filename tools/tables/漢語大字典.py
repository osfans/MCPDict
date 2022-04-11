#!/usr/bin/env python3

from tables._表 import 表 as _表, getCompatibilityVariants
from collections import defaultdict
import re

class 表(_表):
	short = "漢大"
	note = "來源：<a href=https://github.com/zi-phoenicia/hydzd/>GitHub</a>"
	
	def update(self):
		d = defaultdict(list)
		hd = defaultdict(dict)
		numbers="❶❷❸❹❺❻❼❽❾❿⓫⓬⓭⓮⓯⓰⓱⓲⓳⓴㉑㉒㉓㉔㉕㉖㉗㉘㉙㉚㉛㉜㉝㉞㉟㊱㊲㊳㊴㊵㊶㊷㊸㊹㊺㊻㊼㊽㊾㊿"
		kCompatibilityVariants = getCompatibilityVariants()
		pages = dict()
		for line in open(self.spath,encoding="U8"):
			fs = line.strip('\n').split('\t')
			if len(fs[0]) == 1:
				hz,py,js,page = fs[:4]
				if hz in kCompatibilityVariants and js.startswith("同"): continue
				pages[hz] = page
				if py == "None":
					py = ""
				py = py.rstrip("5")
				if py in hd[hz]:
					hd[hz][py].append(js)
				else:
					hd[hz][py] = [js]
		for hz in hd:
			for py in hd[hz]:
				if len(hd[hz][py])!=1:
					hd[hz][py] = [numbers[count]+js for count,js in enumerate(hd[hz][py])]
		for hz in hd:
			js = "\t\t".join(["%s\t%s" % (py, "\t".join(hd[hz][py])) for py in hd[hz]])
			js = re.sub("=(.)", "“\\1”", js).strip()
			d[hz] = ["%s\t%s"%(pages[hz], js)]
		self.write(d)
