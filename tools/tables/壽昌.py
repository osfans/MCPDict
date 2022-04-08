#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "czh_yz_jdsc"
	_file = "建德寿昌白读*.tsv"
	toneValues = {"7a":"7", "7b":"9"}
	
	def format(self, line):
		for i,j in self.toneValues.items():
			line = line.replace("[%s]" % i, "[%s]" % j)
		return line

	def patch(self, d):
		for line in open(self.get_fullname("建德寿昌文读.tsv"),encoding="U8"):
			line = line.strip('\n')
			fs = [i.strip('" ') for i in line.split('\t')]
			if not fs:
				continue
			if fs[0].startswith("#"):
				ym = fs[0][1:]
				continue
			if len(fs) != 2: continue
			sm = fs[0]
			for sd,hzs in re.findall("\[(\d+)\]([^\[\]]+)", fs[1]):
				if sd.isdigit(): sd = "1" + sd
				yb = sm + ym +sd
				hzm = re.findall("(.)\d?(\{.*?\})?", hzs)
				for hz, m in hzm:
					js = m.strip("{}")
					p = f"{yb}=\t{js}"
					if p not in d[hz]:
						d[hz].append(p)
