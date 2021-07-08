#!/usr/bin/env python3

from openpyxl import load_workbook
from collections import defaultdict
import os.path

def get_dict():
	yt = dict()
	if os.path.exists("yt.txt"):
		f = open("yt.txt")
		for line in f:
			fs = line.strip().split(" ")
			yt[int(fs[0])] = fs[1]
		f.close()
	else:
		for sheet in load_workbook("韵图音系同音字表.xlsx"):
			for row in sheet.rows:
				y = row[0].value
				for cell in row[1:]:
					if v := cell.value:
						if type(v) is float:
							yt[int(v)]=y
						elif type(v) is str and "#" in v:
							for i in v.split("#"):
								yt[int(i)]=y
		f = open("yt.txt", "w")
		for i in sorted(yt.keys()):
			y = yt[i]
			t = y[-1]
			if t == "3" : y = y[:-1]+"5"
			elif t == "2" : y = y[:-1]+"3"
			elif t == "1" : y = y[:-1]+"1"
			else: y = y + "7"
			print(i,y,file=f)
		f.close()
	
	pq = dict()
	for line in open("PrengQim.txt"):
		line = line.strip()
		if line.startswith("#"): continue
		fs = line.split(" ")
		pq[fs[1]] = yt[int(fs[0])]

	d=defaultdict(list)
	for line in open("zyenpheng.dict.yaml"):
		line = line.strip()
		fs = line.split('\t')
		if len(fs) < 2: continue
		hz, py = fs[:2]
		if len(hz) == 1 and py in pq:
			d[hz].append(pq[py])
	return d
