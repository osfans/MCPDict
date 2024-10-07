#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表 as _表

class 表(_表):
	disorder = True
	
	def format(self, line):
		name = str(self)
		if name in ("安澤和川",):
			line = re.sub(r"^(.*?)［", "\\1	［", line)
		elif name in ("寶應望直港","羅山周黨","涇縣茂林","沁源", "同江二屯","象山鶴浦","趙縣"):
			line = re.sub(r"^(.*?) ?\[", "\\1	[", line)
		elif name in ("宜昌",):
			line = line.replace('""	"', '"#')
		elif name in ("巢湖",):
			line = line.replace('""	"', '"#').replace("ø","Ø")\
				.replace("（0）","[0]").replace(")","）").replace("（","｛").replace("）","｝")
		elif name in ("羅山",):
			line = re.sub(r"[:：] ?\[", "	[", line).replace("ø","Ø")
		elif name in ("介休張蘭",):
			line = re.sub(r"[\[［](\d)[\]］][）)]","\\1)",line)
		elif name in ("赤壁神山",):
			line = line.replace("", "ᵑ")
		elif name in ("羅田大河岸",):
			line = line.replace("[", "［").replace("", "")
			line = re.sub("^(.*?)［", "\\1	［", line)
		elif name in ("江山廿八都",):
			line = re.sub("([&@])(?!{)","{\\1}",line)
			line = line.replace("&{","{&").replace("@{","{@")
		elif name in ("樅陽","潛山"):
			line = line.replace("*", "□")
		elif name in ("浦城觀前",):
			line = line.replace("", "Ø").replace("", "")
			line = re.sub("^(.*?)［", "\\1	［", line)
		elif name in ("昆明","建水臨安",):
			line = re.sub(r"^.*?\t", "", line)
			line = line.replace("(", "{").replace("〔", "{").replace("（","{").replace(")", "}").replace("）", "}")
		elif name in ("丹鳳","商州","嘉定中","嘉定西","嘉定城","嘉定外","寶山","寶山羅店"):
			if line.startswith("#"): line = "#"
		elif name in ("運城", "興縣"):
			line = line.replace("ø", "")
		elif name in ("雲霄",):
			line = line.replace("（","{").replace("）","}").replace("〉","}")
		return line

	def update(self):
		d = defaultdict(list)
		ym = ""
		skip = self.info.get("跳過行數", 0)
		lineno = 0
		for line in open(self.spath,encoding="U8"):
			lineno += 1
			if lineno <= skip: continue
			line = self.format(line)
			if not line: continue
			line = line.strip().replace('"','').replace("＝","=").replace("－", "-").replace("—","-").replace("｛","{").replace("｝","}").replace("?","？").replace("：[", "	[").replace("{：",'{')
			line = re.sub(r"\[(\d+[a-zA-Z]?)\]", "［\\1］",line)
			line = re.sub("［([^0-9]+.*?)］", "[\\1]",line)
			if "{" not in line and "（" in line:
				line = line.replace("（","{").replace("）","}")
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
			if "\t" not in line: line = re.sub(r"^(.*?)\［", "\\1	［", line)
			fs = line.split("\t")[:2]
			if len(fs) != 2: continue
			sm = fs[0].strip()
			for sd,hzs in re.findall(r"［(\d+[a-zA-Z]?)］([^［］]+)", fs[1]):
				py = sm + ym +sd
				hzs = re.findall(r"(.)\d?([<+\-/=\\\*？$&r]?)\d?(\{.*?\})?", hzs)
				for hz, c, js in hzs:
					if hz == " ": continue
					p = ""
					if c:
						if c in "+-*/=\\":
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
							elif c == '<':
								p = "(舊)"
								c = ""
					js = js.strip("{}")
					p = py + c + "\t" + p + js
					if p not in d[hz]:
						d[hz].append(p)
		self.write(d)
