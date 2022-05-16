#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表 as _表

class 表(_表):
	disorder = True
	def update(self):
		d = defaultdict(list)
		ym = ""
		skip = self.info.get("跳過行數", 0)
		lineno = 0
		for line in open(self.spath,encoding="U8"):
			lineno += 1
			if lineno <= skip: continue
			line = self.format(line)
			if str(self) in ("丹鳳","商州") and line.startswith("#"): continue
			line = line.strip().replace('"','').replace("＝","=").replace("－", "-").replace("—","-").replace("｛","{").replace("｝","}").replace("?","？").replace("：[", "	[").replace("{：",'{')
			line = re.sub("\[(\d+[a-zA-Z]?)\]", "［\\1］",line)
			line = re.sub("［([^0-9]+.*?)］", "[\\1]",line)
			if not line: continue
			line = line.lstrip(" ")
			if line.startswith("	#"): line = line[1:]
			if line.startswith("#"):
				ym = line[1:]
				if not ym: continue
				ym = ym.split("\t")[0].strip()
				continue
			if "［" not in line and re.match(".*[①-⑨]", line):
				for i in range(1,10):
					sda = chr(ord('①') + (i - 1))
					sdb = f"［{i}］"
					line = line.replace(sda, sdb)
			if "{" not in line and "（" in line:
				line = line.replace("（","{").replace("）","}")
			if "\t" not in line: line = re.sub("^(.*?)\［", "\\1	［", line)
			fs = line.split("\t")[:2]
			if len(fs) != 2: continue
			sm = fs[0].strip()
			lstrip = str(self) in ("運城", "興縣")
			for sd,hzs in re.findall("［(\d+[a-zA-Z]?)］([^［］]+)", fs[1]):
				if sd == "0": sd = ""
				py = sm + ym +sd
				if lstrip: py = py.lstrip("ø")
				hzs = re.findall("(.)\d?([+\-/=~≈\\\*？$&r]?)\d?(\{.*?\})?", hzs)
				for hz, c, js in hzs:
					if hz == " ": continue
					p = ""
					if c:
						if c in "+-*/=~≈\\":
							pass
						else:
							if c == '？':
								p = ""
								c = "?"
							elif c == '$':
								p = "(单字调)"
								c = ""
							elif c == '&':
								p = "(连读前字调)"
								c = ""
							elif c == 'r':
								p = "(兒化)"
								c = ""
					js = js.strip("{}")
					p = py + c + "\t" + p + js
					if p not in d[hz]:
						d[hz].append(p)
		self.write(d)
