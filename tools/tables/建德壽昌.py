#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "czh_yz_jdsc"
	note = "來源：<u>清竮塵</u>整理自曹志耘《徽語嚴州方言研究》<br>說明：文白讀調值不同"
	tones = "112 1 1a 陰平 ꜀,52 2 1b 陽平 ꜁,324 3 2a 陰上 ꜂,53 4 2b 陽上 ꜃,334 5 3 去 ꜄,,55 7a 4a 上陰入 ꜆,12 8a 4c 陽入 ꜇,3 7b 4b 下陰入 ꜀,,334 1b 1c 陰平 ꜀,112 2b 1d 陽平 ꜁,55 3b 2b 上 ꜂,,,324 6 3b 去 ꜄,5 7c 4d 陰入 ꜆,13 8b 4e 陽入 ꜇"
	_file = "建德寿昌白读*.tsv"
	toneValues = {"7a":"7", "7b":"9"}
	simplified = 2
	
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
			sm = fs[0].replace("Ø", "")
			for sd,hzs in re.findall("\[(\d+)\]([^\[\]]+)", fs[1]):
				if sd.isdigit(): sd = "1" + sd
				yb = sm + ym +sd
				hzm = re.findall("(.)\d?(\{.*?\})?", hzs)
				for hz, m in hzm:
					js = m.strip("{}")
					p = f"{yb}=\t{js}"
					if p not in d[hz]:
						d[hz].append(p)
