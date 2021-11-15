#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表

class 字表(表):
	key = "cmn_xn_wmgh"
	_color = "#C600FF,#FFAD00"
	_file = "武鸣官话同音字表20211009.tsv"
	note = "版本：2021-12-02<br>來源：<u>清竮塵</u>整理自陸淼焱《武鳴縣城官話調查報告》"
	tones = "33 1 1a 陰平 ꜀,21 2 1b 陽平 ꜁,55 3 2 上 ꜂,,24 5 3 去 ꜄,,55 7a 4a 高入 ꜆,21 7b 4b 低入 ꜀,35   借入調 "

	def update(self):
		d = defaultdict(list)
		for line in open(self.spath):
			line = line.strip('\n')
			fs = [i.strip('" ') for i in line.split('\t')]
			if not fs: continue
			if fs[0].startswith("#"):
				ym = fs[0][1:]
				continue
			if len(fs) < 2: continue
			sm = fs[0]
			for sd,hzs in re.findall("\[(\d+[ab]?)\]([^\[\]]+)", fs[1]):
				if sd == "7a": sd = "7"
				elif sd == "7b": sd = "8"
				py = sm + ym + sd
				hzm = re.findall("(.)\d?([-=])?(\{.*?\})?", hzs)
				for hz, c, m in hzm:
					m = m.strip("{}")
					p = ""
					if c and c in '-=':
						if c == '-':
							p = "白"
						elif c == '=':
							p = "文"
					if p and m:
						p = "(%s)"%p
					p = p + m
					p = "%s\t%s" % (py, p)
					if p not in d[hz]:
						d[hz].append(p)
		self.write(d)

