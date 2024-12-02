#!/usr/bin/env python3

from tables._縣志 import 表 as _表
import re

class 表(_表):
	def patch(self, d):
		for line in open(self.get_fullname("建德寿昌文读.tsv"),encoding="U8"):
			line = line.strip('\n')
			fs = [i.strip() for i in line.split('\t')]
			if not fs:
				continue
			if fs[0].startswith("#"):
				ym = fs[0][1:]
				continue
			if len(fs) != 2: continue
			sm = fs[0]
			for sd,hzs in re.findall(r"\[(\d+)\]([^\[\]]+)", fs[1]):
				if sd.isdigit(): sd = sd + "d"
				yb = sm + ym +sd
				hzm = re.findall(r"(.)\d?(\{.*?\})?", hzs)
				for hz, m in hzm:
					js = m.strip("{}")
					p = f"{yb}=\t{js}"
					if p not in d[hz]:
						d[hz].append(p)
