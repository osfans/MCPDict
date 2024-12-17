#!/usr/bin/env python3

import re, regex
from collections import defaultdict
from tables._表 import 表 as _表

def 常熟古裡_repl(match):
	line = match.group(0)
	if re.match(".*[①-⑨]+", line):
		for i in range(1,10):
			sda = chr(ord('①') + (i - 1))
			sdb = f"{i}"
			line = line.replace(sda, sdb)
	return line

class 表(_表):
	disorder = True
	sm = ""
	ym = ""
	ym2 = ""
	
	def format(self, line):
		name = str(self)
		if name in ("安澤和川",):
			line = re.sub(r"^(.*?)［", "\\1	［", line)
		elif name in ("寶應望直港","羅山周黨","涇縣茂林","沁源", "同江二屯","象山鶴浦","趙縣"):
			line = re.sub(r"^(.*?) ?\[", "\\1	[", line)
		elif name in ("萍鄕","平陽","都昌陽峯"):
			line = line.lstrip("∅︀")
		elif name in ("遂川","大庸","大庸三眼橋", "婺川"):
			line = re.sub(r"\[(\d+)\]", lambda x:"[%s]"%self.dz2dl(x[1]), line)
		elif name in ("奉化",):
			line = re.sub(r"(\d+)(?![：\d])", lambda x:"[%s]"%self.dz2dl(x[1]), line)
			line = line.lstrip("q")
		elif name in ("巢湖",):
			line = line.replace("ø","Ø").replace("（0）","[0]")
			line = self.normS(line, "{\\1}")
		elif name in ("崇仁"):
			line = self.normS(line, "{\\1}")
		elif name in ("羅山","贛縣安平"):
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
		elif name in ("樅陽","潛山","靑陽客籍話"):
			line = line.replace("*", "□")
		elif name in ("樅陽東",):
			line = line.replace("*", "□")
			line = self.normS(line, "{\\1}")
			line = re.sub("[가-힣]+[, ]*", "", line).lstrip()
			if line.startswith("#"):
				line = re.sub('^(#[^ ]*) .*?	', '\\1', line)
			elif "[" in line:
				line = re.sub(r'(.*?)[/ ].*?	(\[.+)$', '\\1	\\2', line)
			else:
				line = ""
		elif name in ("臨髙話",):
			if " " not in line: return "#"
			line = line.strip()
			line = re.sub(r"<(.*?)>","\\1{讀書音}",line)
			line = re.sub(r"\[(.*?)\]","\\1{特殊音}",line)
			line = re.sub(r"(.)\*","\\1{海口話影響}",line)
			line = re.sub(r"([1-5])", "[\\1]", line)
			line = re.sub(r"([ptk]) ", "\\1 [5]", line)
			line = re.sub(r"^(.*?)\[", "\\1	[", line)
			line = line.replace(" ", "")
		elif name in ("浦城觀前",):
			line = line.replace("", "Ø").replace("", "")
			while (newline := re.sub(r"(?<=‖)([^［］]*[^‖]){", "\\1‖{", line)) != line:
				line = newline
			line = re.sub("‖{", "{(連讀音)", line)
		elif name in ("福鼎白琳",):
			line = re.sub(r"(‖)(\[\d+\])", "\\2\\1",line)
			while (newline := re.sub(r"(?<=‖)([^\[\]]*[^‖]){", "\\1‖{", line)) != line:
				line = newline
			line = re.sub("‖{", "{(連讀音)", line)
		elif name in ("建德"):
			line = re.sub(r"\t2\d.*$", "", line)
		elif name in ("屯溪船上話"):
			line = re.sub(r"连读.*$", "", line)
		elif name in ("潼關太要",):
			if line.startswith("["): line = ""
		elif name in ("昆明","建水臨安",):
			if line.startswith("\t\t"): line = ""
			line = re.sub(r"^.*?\t", "", line)
			line = line.replace("(", "{").replace("〔", "{").replace("（","{").replace(")", "}").replace("）", "}")
		elif name in ("丹鳳","嘉定中","嘉定西","嘉定城","嘉定外","寶山","寶山羅店","南皮"):
			if line.startswith("#"): line = "#"
		elif name in ("商州",):
			if line.startswith("#"): line = "#"
			line = re.sub(r"\[([^\d]+)\]", "\\1", line)
		elif name in ("運城", "興縣"):
			line = line.replace("ø", "")
		elif name in ("永定", "連城四堡", "上杭古田"):
			line = line.replace("*", "@")
		elif name in ("雲霄",):
			line = line.replace("〉","）")
			line = self.normS(line, "{\\1}")
		elif name in ("通道菁蕪洲",):
			line = re.sub("([&])(?!{)","{西官借詞}",line).replace("&{","{(西官借詞)")
		elif name in ("泰興",):
			line = line.lstrip("q")
		elif name in ("壺關樹掌"):
			line = line.lstrip("q").replace("·", "0")
			line = re.sub(r"\[(\d+)\]", lambda x:"[%s]"%self.dz2dl(x[1]), line)
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
			line = line.replace("[","［").replace("]","］")
			line = line.replace("*（", "□（")
			line = self.normS(line, "{\\1}")
			line = re.sub(r"\*(.)", "\\1?", line)
			line = re.sub(r"［(.)(.*?)］", "\\1*\\2", line)
			fs = line.split("\t")
			for i,sd in enumerate(self.toneMaps.values()):
				if fs[i + 1]:
					fs[i + 1] = f"[{sd}]" + fs[i + 1]
			line = "".join(fs)
		elif name in ("虔南大吉山",):
			line = re.sub(r"\[.*?(\d+)\]", lambda x:"[%s]"%self.dz2dl(x[1]), line)
			line = line.replace("<","{").replace(">","}")
		elif name in ("澄海大新","光山", "南康唐江", "仁化長江", "永豐", "南豐"):
			line = re.sub(r"\[(\d+)\]", lambda x:"[%s]"%self.dz2dl(x[1]), line)
		elif name in ("耒陽",):
			line = line.replace("51", "53")
			line = re.sub(r"\[(\d+)\]", lambda x:"[%s]"%self.dz2dl(x[1]), line)
		elif name in ("建湖卞港",):
			line = line.replace("[2]", "[23-2]")
			line = re.sub(r"\[([\d\-]+)\]", lambda x:"[%s]"%self.dz2dl(x[1]), line)
		elif name in ("慈利",):
			line = re.sub(r"\[(\d+)\]", lambda x:"[%s]"%self.dz2dl(x[1]), line)
			line = line.replace("/", "")
		elif name in ("海門"):
			if line.startswith("#"): return "#"
		elif name in ("博白","東莞塘角"):
			if line.startswith("#"): return "#"
			find = re.findall(r"\[(.*?)(\d+)\]", line)
			if not find: return
			sy = find[0][0]
			line = re.sub(r"\[(.*?)(\d+)\]", lambda x:"[%s]"%self.dz2dl(x[2]), line)
			line = sy + line
		elif name in ("小店", "太谷", "祁縣", "壽陽", "楡次", "徐溝"):
			fs = line.split("\t", 1)
			fs[1] = fs[1].replace("\t", "")
			line = "\t".join(fs)
		elif name in ("東干語",):
			if line.startswith("#"):
				yms = line.rstrip().replace("#", "").split("\t")
				if len(yms) != 2: return
				ym, ym2 = yms
				self.ym2 = ym2
				return f"#{ym}"
			sms = line.split("\t", 2)
			sm, sm2, hzs = sms
			sm = f"{sm2}{self.ym2}/{sm}".replace("Ø", "")
			return f"{sm}\t{hzs}"
		elif name in ("敦煌", "洛陽"):
			line = re.sub(r"\[(\d+)\]", lambda x: "[%s]"%self.dz2dl(x[1]), line)\
				.replace("(", "（").replace(")", "）").replace("\t", "").rstrip("12345 \t\n")
			line = re.sub(r"\[([^\d].*?)\]", "（\\1）", line)
			line = regex.sub("（((?>[^（）]+|(?R))*)）", "{\\1}", line)
		elif name in ("博羅",):
			if "[" not in line and not line.startswith("#"): line = ""
			line = re.sub(r"\[(\d+)\]", lambda x: "["+self.dz2dl(x[1])+"]", line)
			line = self.normS(line, "{\\1}")
			line = line.lstrip("ø")
		elif name in ("金壇",):
			if line.strip().endswith("韻"): line = ""
		elif name in ("大豐三龍"):
			if "\t" in line:
				if line.startswith("\t"):
					line = self.sm + line
				else:
					self.sm = line.split("\t")[0]
				line = re.sub(r"\[(\d+)\]", lambda x:"[%s]"%self.dz2dl(x[1]), line)
		elif name in ("烏魯木齊", "西寧"):
			line = re.sub(r"(\d+)", "[\\1]", line)
			if line.startswith("["):
				line = self.sm + line
			else:
				self.sm = line.split("[")[0]
			line = line.lstrip("q")
		elif name in ("天台城關"):
			line = re.sub(r"(\d)", "[\\1]", line)
			line = re.sub(r"^(.*?)(\[)", "\\1	\\2", line)
			line = self.normS(line, "{\\1}")
			if "[" not in line: line = ""
		elif name in ("南昌"):
			line = line.replace("\t", "")
			line = re.sub(r"^(.*?)(\[)", "\\1	\\2", line)
			line = self.normS(line, "{\\1}")
		elif name in ("髙郵"):
			line = line.replace("ⓘ", "①").replace("Ⓘ", "①")
			line = line.replace("➀", "①").replace("➁", "②").replace("➂","③").replace("➃", "④").replace("➄", "⑤")
			line = line.lstrip("q")
			line = line.replace("-", "(新派錯音)")
		elif name in ("賀州南鄕"):
			line = line.lstrip("ø")
		elif name in ("南京老派"):
			line = re.sub("([，。])(（)", "\\2\\1", line)
			line = line.replace("，", "（又）").replace("。", "（新）")
			line = self.normS(line, "{\\1}")
			line = re.sub(r"(\{[^{}]+?)（又）([^{}]*?\})", "\\1，\\2", line)
			line = re.sub(r"(\{[^{}]+?)（新）([^{}]*?\})", "\\1。\\2", line)
		elif name in ("常熟古裡",):
			line = re.sub(r"\{[^{}]*?[①-⑨][^{}]*?\}", 常熟古裡_repl, line)
		elif name in ("松江天馬",):
			line = line.lstrip("ø")
		elif name in ("句容",):
			if re.match(".*[①-⑨ⓐⓑ]+", line):
				for i in range(1,10):
					sda = chr(ord('①') + (i - 1))
					sdb = f"［{i}］"
					line = line.replace(sda, sdb)
			line = line.replace("］ⓐ", "a］").replace("］ⓑ", "b］")
		elif name in ("休寧",):
			line = line.replace("[3ˀ]", "[3]")
		elif name in ("光澤寨裏",):
			line = line.replace("‖", "")
		elif name in ("泰州",):
			line = line.replace("'", "ʰ")
			line = re.sub("([-=])(.)", "\\2\\1", line)
		elif name in ("吉水金灘", "繁昌"):
			line = re.sub("([mnvʋl])([\u0329\u030D]+)", "\\1\u0329", line)
			line = re.sub("([ŋȵʐɱɻʒ])([\u0329\u030D]+)", "\\1\u030D", line)
		elif name in ("贛楡", "徐州", "銀川", "大同", "儀徵"):
			line = line.strip().replace(",","，").replace(";","；").replace(":","：").replace("？（", "□（")
			line = line.lstrip("øq")
			if line.startswith("#"): return line
			line = re.sub(r"([？#\-\+])(.)", "\\2\\1", line)
			line = line.replace("-", "(舊)").replace("+", "/").replace("？", "?").replace("#", "*")
		return line

	def parseYm(self, line):
		ym = None
		line = line.strip()
		if not line: return ym
		if line.startswith("#"): line = line[1:]
		elif "［" in line or "］" in line: return ym
		ym = line
		if ym:
			ym = ym.split("\t")[0].strip().strip("[]")
		return ym
	
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
			line = line.strip().replace("＝","=").replace("－", "-").replace("—","-").replace("｛","{").replace("｝","}").replace("?","？").replace("：[", "	[").replace("{：",'{')
			line = line.replace("[·]", "[0]")
			line = re.sub(r"\[(\d+[a-zA-Z]?)\]", "［\\1］",line)
			line = re.sub("［([^0-9]+.*?)］", "[\\1]",line)
			if ("{" not in line and "｛" not in line) and ("（" in line or "(" in line):
				line = self.normS(line, "{\\1}")
			line = line.lstrip(" ")
			if "［" not in line and re.match(".*[①-⑨]", line):
				for i in range(1,10):
					sda = chr(ord('①') + (i - 1))
					sdb = f"［{i}］"
					line = line.replace(sda, sdb)
			if (s := self.parseYm(line)) is not None:
				ym = s
				continue
			matches = re.findall("^([^［］]*?)(［.+)$", line)
			if not matches or len(matches[0]) != 2: continue
			fs = list(matches[0])
			fs[0] = fs[0].strip().strip("[]")
			fs[1] = fs[1].replace("\t", "")
			fs[1] = re.sub(r" (\d)", "\\1", fs[1])
			sm, hzs = fs
			pys = set()
			for sd,hzs in re.findall(r"［(\d+[a-zA-Z]?)］([^［］]+)", fs[1]):
				py = sm + ym +sd
				if py not in pys:
					pys.add(py)
				else:
					self.errors.append(f"{py} 重複")
				hzs = self.normG(hzs)
				hzs = re.findall(r"(.)[\d₁₂₃]?([<+\-/=\\\*？$&r@]?)[\d₁₂₃]? *(｛.*?｝)?", hzs)
				for hz, c, js in hzs:
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
						self.errors.append(f"大括號未成對:{js}")
						js = js.replace("{", "").replace("}", "")
					p = py + c + "\t" + p + js
					if p not in d[hz]:
						d[hz].append(p)
		self.write(d)
