#!/usr/bin/env python3

from tables._縣志 import 表 as _表
import re

class 表(_表):
	def patch(自, d):
		for 行 in open(自.全路徑("建德寿昌文读.tsv"),encoding="U8"):
			行 = 行.strip('\n')
			列 = [i.strip() for i in 行.split('\t')]
			if not 列:
				continue
			if 列[0].startswith("#"):
				ym = 列[0][1:]
				continue
			if len(列) != 2: continue
			sm = 列[0]
			for sd,字組 in re.findall(r"\[(\d+)\]([^\[\]]+)", 列[1]):
				if sd.isdigit(): sd = sd + "d"
				yb = sm + ym +sd
				hzm = re.findall(r"(.)\d?(\{.*?\})?", 字組)
				for 字, m in hzm:
					js = m.strip("{}")
					p = f"{yb}=\t{js}"
					if p not in d[字]:
						d[字].append(p)
