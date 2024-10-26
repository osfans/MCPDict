#!/usr/bin/env python3

import re, regex
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
		elif name in ("丹鳳","商州","嘉定中","嘉定西","嘉定城","嘉定外","寶山","寶山羅店","南皮"):
			if line.startswith("#"): line = "#"
		elif name in ("運城", "興縣"):
			line = line.replace("ø", "")
		elif name in ("永定", "連城四堡", "上杭古田"):
			line = line.replace("*", "@")
		elif name in ("雲霄",):
			line = line.replace("（","{").replace("）","}").replace("〉","}")
		elif name in ("道縣梅花",):
			#!西官陰平藉詞@西官陽平藉詞$西官上聲藉詞%西官去聲藉詞
			line = re.sub("(!)(?!{)","{西官陰平借詞}",line)
			line = line.replace("!{","{(西官陰平借詞)")
			line = re.sub("(@)(?!{)","{西官陽平借詞}",line)
			line = line.replace("@{","{(西官陽平借詞)")
			line = re.sub(r"(\$)(?!{)","{西官上聲借詞}",line)
			line = line.replace("${","{(西官上聲借詞)")
			line = re.sub("(%)(?!{)","{西官去聲借詞}",line)
			line = line.replace("%{","{(西官去聲借詞)")
		elif name in ("連城文保", "長汀"):
			if line.startswith("#"): return line
			line = line.replace("(","（").replace(")","）")
			line = line.replace("[","［").replace("]","］")
			line = line.replace("*（", "□（")
			line = regex.sub("（((?>[^（）]+|(?R))*)）", "{\\1}", line)
			line = re.sub(r"\*(.)", "\\1?", line)
			line = re.sub(r"［(.)(.*?)］", "\\1*\\2", line)
			fs = line.split("\t")
			for i,sd in enumerate(self.toneMaps.values()):
				if fs[i + 1]:
					fs[i + 1] = f"[{sd}]" + fs[i + 1]
			line = "".join(fs)
		elif name in ("光山",):
			line = re.sub(r"\[(\d+)\]", lambda x:f"[{self.toneMaps[x[1]]}]", line)
		elif name in ("博白","東莞塘角"):
			if line.startswith("#"): return "#"
			find = re.findall(r"\[(.*?)(\d+)\]", line)
			if not find: return
			sy = find[0][0]
			line = re.sub(r"\[(.*?)(\d+)\]", lambda x:f"[{self.toneMaps[x[2]]}]", line)
			line = sy + line
		elif name in ("小店", "太谷", "祁縣", "壽陽", "楡次", "徐溝"):
			fs = line.split("\t", 1)
			fs[1] = fs[1].replace("\t", "")
			line = "\t".join(fs)
		elif name in ("江門荷塘(下)",):
			if line.startswith("#"): return "#"
			fs = line.split("\t", 2)
			sm, ym = fs[:2]
			line = line.replace("(", "（").replace(")", "）").replace("[", "〔").replace("]", "〕")
			line = re.sub(r"（([^（）]*?)）〔([^〔〕]*?)〕", "{\\1：\\2}", line)
			line = re.sub(r"〔([^〔〕]*?)〕（([^（）]*?)）", "{\\1：\\2}", line)
			line = re.sub(r"（(.*?)）", "{\\1}", line)
			line = re.sub(r"〔(.*?)〕", "{\\1}", line)
			line = line.replace("}{", "；")
			line = re.sub(r"\t(\d+)", lambda x: "["+ self.dz2dl(ym + x[1]).replace(ym, "") +"]", line)
			line = line.replace("\t" + ym, ym + "\t")
			fs = line.split("\t", 1)
			line = fs[0] + "\t" + fs[1].replace("\t", "")
		elif name in ("敦煌", "洛陽"):
			line = re.sub(r"\[(\d+)\]", lambda x: "["+self.dz2dl(x[1])+"]", line)\
				.replace("(", "（").replace(")", "）").replace("\t", "").rstrip("12345 \t\n")
			line = re.sub(r"\[([^\d].*?)\]", "（\\1）", line)
			line = regex.sub("（((?>[^（）]+|(?R))*)）", "{\\1}", line)
		elif name in ("句容",):
			if re.match(".*[①-⑨ⓐⓑ]+", line):
				for i in range(1,10):
					sda = chr(ord('①') + (i - 1))
					sdb = f"［{i}］"
					line = line.replace(sda, sdb)
			line = line.replace("］ⓐ", "a］").replace("］ⓑ", "b］")
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
				ym = ym.split("\t")[0].strip().strip("[]")
				continue
			if "［" not in line and re.match(".*[①-⑨]", line):
				for i in range(1,10):
					sda = chr(ord('①') + (i - 1))
					sdb = f"［{i}］"
					line = line.replace(sda, sdb)
			if "\t" not in line: line = re.sub(r"^(.*?)\［", "\\1	［", line)
			fs = line.split("\t")[:2]
			if len(fs) != 2: continue
			sm = fs[0].strip().strip("[]")
			for sd,hzs in re.findall(r"［(\d+[a-zA-Z]?)］([^［］]+)", fs[1]):
				py = sm + ym +sd
				hzs = regex.findall(r"(.)\d?([<+\-/=\\\*？$&r@]?)\d?(\{((?=[^\{\}]*)|(?R))*?\})?", hzs)
				for hz, c, js, _ in hzs:
					if hz == " ": continue
					p = ""
					if c:
						if c in "+-*/=@\\":
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
					js = js[1:-1]
					if js.count("{") != js.count("}"):
						print("\t\t\tnot paired:", js)
						js = js.replace("{", "").replace("}", "")
					p = py + c + "\t" + p + js
					if p not in d[hz]:
						d[hz].append(p)
		self.write(d)
