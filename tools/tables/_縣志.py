#!/usr/bin/env python3

import re, regex
from collections import defaultdict
from tables._表 import 表 as _表

def 常熟古裡_repl(match):
	行 = match.group(0)
	if re.match(".*[①-⑨]+", 行):
		for i in range(1,10):
			sda = chr(ord('①') + (i - 1))
			sdb = f"{i}"
			行 = 行.replace(sda, sdb)
	return 行

class 表(_表):
	註序 = True
	聲 = ""
	韻 = ""
	ym2 = ""
	
	def 統(自, 行):
		行 = _表.統(自, 行)
		名 = str(自)
		if 名 in ("永州嵐角山", "賀州南鄕", "松江天馬", "運城", "興縣","豐城","豐城鐵路","新建","賀州江坪"):
			行 = 行.lstrip("ø")
		elif 名 in ("江夏湖泗"):
			行 = 行.replace("ø[", "0[")
		elif 名 in ("遂川","大庸南","大庸北", "婺川", "蒙山程村","欽州東場"):
			行 = re.sub(r"\[(\d+)\]", lambda x:"[%s]"%自.dz2dlWithYm(x[1], 自.韻), 行)
		elif 名 in ("奉化",):
			行 = re.sub(r"(\d+)(?![：\d])", lambda x:"[%s]"%自.dz2dl(x[1]), 行)
			行 = 行.lstrip("q")
		elif 名 in ("巢湖",):
			行 = 行.replace("ø","Ø").replace("（0）","[0]")
			行 = 自.normS(行, "{\\1}")
		elif 名 in ("崇仁"):
			行 = 自.normS(行, "{\\1}")
		elif 名 in ("羅山","贛縣安平"):
			行 = re.sub(r"[:：] ?\[", "	[", 行).replace("ø","Ø")
		elif 名 in ("介休張蘭",):
			行 = re.sub(r"[\[［](\d)[\]］][）)]","\\1)",行)
		elif 名 in ("赤壁神山",):
			行 = 行.replace("", "ᵑ")
		elif 名 in ("羅田大河岸",):
			行 = 行.replace("[", "［").replace("", "")
			行 = re.sub("^(.*?)［", "\\1	［", 行)
		elif 名 in ("江山廿八都",):
			行 = re.sub("([&@])(?!{)","{\\1}",行)
			行 = 行.replace("&{","{&").replace("@{","{@")
		elif 名 in ("樅陽","潛山","靑陽客籍話"):
			行 = 行.replace("*", "□")
		elif 名 in ("樅陽東",):
			行 = 行.replace("*", "□")
			行 = 自.normS(行, "{\\1}")
			行 = re.sub("[가-힣]+[, ]*", "", 行).lstrip()
			if 行.startswith("#"):
				行 = re.sub('^(#[^ ]*) .*?	', '\\1', 行)
			elif "[" in 行:
				行 = re.sub(r'(.*?)[/ ].*?	(\[.+)$', '\\1	\\2', 行)
			else:
				行 = ""
		elif 名 in ("臨髙話",):
			if " " not in 行: return "#"
			行 = 行.strip()
			行 = re.sub(r"<(.*?)>","\\1{讀書音}",行)
			行 = re.sub(r"\[(.*?)\]","\\1{特殊音}",行)
			行 = re.sub(r"(.)\*","\\1{海口話影響}",行)
			行 = re.sub(r"([1-5])", "[\\1]", 行)
			行 = re.sub(r"([ptk]) ", "\\1 [5]", 行)
			行 = re.sub(r"^(.*?)\[", "\\1	[", 行)
			行 = 行.replace(" ", "")
		elif 名 in ("浦城觀前",):
			行 = 行.replace("", "Ø").replace("", "")
			while (newline := re.sub(r"(?<=‖)([^［］]*[^‖]){", "\\1‖{", 行)) != 行:
				行 = newline
			行 = re.sub("‖{", "{(連讀音)", 行).replace("‖", "")
		elif 名 in ("福鼎白琳",):
			行 = re.sub(r"(‖)(\[\d+\])", "\\2\\1",行)
			while (newline := re.sub(r"(?<=‖)([^\[\]]*[^‖]){", "\\1‖{", 行)) != 行:
				行 = newline
			行 = re.sub("‖{", "{(連讀音)", 行).replace("‖", "")
		elif 名 in ("建德"):
			行 = re.sub(r"\t2\d.*$", "", 行)
		elif 名 in ("屯溪船上話"):
			行 = re.sub(r"连读.*$", "", 行)
		elif 名 in ("潼關太要",):
			if 行.startswith("["): 行 = ""
		elif 名 in ("昆明","建水臨安",):
			if 行.startswith("\t\t"): 行 = ""
			行 = re.sub(r"^.*?\t", "", 行)
			行 = 行.replace("(", "{").replace("〔", "{").replace("（","{").replace(")", "}").replace("）", "}")
		elif 名 in ("丹鳳","嘉定中","嘉定西","嘉定城","嘉定外","寶山","寶山羅店","南皮"):
			if 行.startswith("#"): 行 = "#"
		elif 名 in ("商州",):
			if 行.startswith("#"): 行 = "#"
			行 = re.sub(r"\[([^\d]+)\]", "\\1", 行)
		elif 名 in ("永定", "連城四堡", "上杭古田"):
			行 = 行.replace("*", "@")
		elif 名 in ("雲霄",):
			行 = 行.replace("〉","）")
			行 = 自.normS(行, "{\\1}")
		elif 名 in ("通道菁蕪洲",):
			行 = re.sub("([&])(?!{)","{西官借詞}",行).replace("&{","{(西官借詞)")
		elif 名 in ("泰興","無爲牛埠","淮陰"):
			行 = 行.lstrip("q")
		elif 名 in ("壺關樹掌"):
			行 = 行.lstrip("q").replace("·", "0")
			行 = re.sub(r"\[(\d+)\]", lambda x:"[%s]"%自.dz2dl(x[1]), 行)
		elif 名 in ("道縣梅花",):
			#!西官陰平藉詞@西官陽平藉詞$西官上聲藉詞%西官去聲藉詞
			行 = re.sub("(!)(?!{)","{西官陰平借詞}",行)
			行 = 行.replace("!{","{(西官陰平借詞)")
			行 = re.sub("(@)(?!{)","{西官陽平借詞}",行)
			行 = 行.replace("@{","{(西官陽平借詞)")
			行 = re.sub(r"(\$)(?!{)","{西官上聲借詞}",行)
			行 = 行.replace("${","{(西官上聲借詞)")
			行 = re.sub("(%)(?!{)","{西官去聲借詞}",行)
			行 = 行.replace("%{","{(西官去聲借詞)")
		elif 名 in ("連城文保", "長汀"):
			if 行.startswith("#"): return 行
			行 = 行.replace("[","［").replace("]","］")
			行 = 行.replace("*（", "□（")
			行 = 自.normS(行, "{\\1}")
			行 = re.sub(r"\*(.)", "\\1?", 行)
			行 = re.sub(r"［(.)(.*?)］", "\\1*\\2", 行)
			列 = 行.split("\t")
			for i,sd in enumerate(自.toneMaps.values()):
				if 列[i + 1]:
					列[i + 1] = f"[{sd}]" + 列[i + 1]
			行 = "".join(列)
		elif 名 in ("虔南大吉山",):
			行 = re.sub(r"\[.*?(\d+)\]", lambda x:"[%s]"%自.dz2dl(x[1]), 行)
			行 = 行.replace("<","{").replace(">","}")
		elif 名 in ("澄海大新","光山", "南康唐江", "仁化長江", "永豐", "南豐","崇左大新"):
			行 = re.sub(r"\[(\d+)\]", lambda x:"[%s]"%自.dz2dl(x[1]), 行)
		elif 名 in ("耒陽",):
			行 = 行.replace("51", "53")
			行 = re.sub(r"\[(\d+)\]", lambda x:"[%s]"%自.dz2dl(x[1]), 行)
		elif 名 in ("建湖卞港",):
			行 = 行.replace("[2]", "[23-2]")
			行 = re.sub(r"\[([\d\-]+)\]", lambda x:"[%s]"%自.dz2dl(x[1]), 行)
		elif 名 in ("慈利",):
			行 = re.sub(r"\[(\d+)\]", lambda x:"[%s]"%自.dz2dl(x[1]), 行)
			行 = 行.replace("/", "")
		elif 名 in ("海門"):
			if 行.startswith("#"): return "#"
		elif 名 in ("博白","東莞塘角"):
			if 行.startswith("#"): return "#"
			find = re.findall(r"\[(.*?)(\d+)\]", 行)
			if not find: return
			sy = find[0][0]
			行 = re.sub(r"\[(.*?)(\d+)\]", lambda x:"[%s]"%自.dz2dl(x[2]), 行)
			行 = sy + 行
		elif 名 in ("東干語",):
			if 行.startswith("#"):
				yms = 行.rstrip().replace("#", "").split("\t")
				if len(yms) != 2: return
				韻, ym2 = yms
				自.ym2 = ym2
				return f"#{韻}"
			聲, sm2, 字組 = 行.split("\t", 2)
			聲 = f"{sm2}{自.ym2}/{聲}".replace("Ø", "")
			return f"{聲}\t{字組}"
		elif 名 in ("敦煌", "洛陽"):
			行 = re.sub(r"\[(\d+)\]", lambda x: "[%s]"%自.dz2dl(x[1]), 行)\
				.replace("(", "（").replace(")", "）").replace("\t", "").rstrip("12345 \t\n")
			行 = re.sub(r"\[([^\d].*?)\]", "（\\1）", 行)
			行 = regex.sub("（((?>[^（）]+|(?R))*)）", "{\\1}", 行)
		elif 名 in ("博羅",):
			if "[" not in 行 and not 行.startswith("#"): 行 = ""
			行 = re.sub(r"\[(\d+)\]", lambda x: "["+自.dz2dl(x[1])+"]", 行)
			行 = 自.normS(行, "{\\1}")
			行 = 行.lstrip("ø")
		elif 名 in ("金壇",):
			if 行.strip().endswith("韻"): 行 = ""
		elif 名 in ("大豐三龍"):
			if "\t" in 行:
				if 行.startswith("\t"):
					行 = 自.聲 + 行
				else:
					自.聲 = 行.split("\t")[0]
				行 = re.sub(r"\[(\d+)\]", lambda x:"[%s]"%自.dz2dl(x[1]), 行)
		elif 名 in ("烏魯木齊", "西寧","蒙山"):
			行 = re.sub(r"(\d+)", "[\\1]", 行, count=1)
			if 行.startswith("["):
				行 = 自.聲 + 行
			else:
				自.聲 = 行.split("[")[0]
			行 = 行.lstrip("q")
		elif 名 in ("天台城關"):
			行 = re.sub(r"(\d)", "[\\1]", 行)
			行 = re.sub(r"^(.*?)(\[)", "\\1	\\2", 行)
			行 = 自.normS(行, "{\\1}")
			if "[" not in 行: 行 = ""
		elif 名 in ("南昌"):
			行 = 行.replace("\t", "")
			行 = re.sub(r"^(.*?)(\[)", "\\1	\\2", 行)
			行 = 自.normS(行, "{\\1}")
		elif 名 in ("髙郵"):
			行 = 行.replace("ⓘ", "①").replace("Ⓘ", "①")
			行 = 行.replace("➀", "①").replace("➁", "②").replace("➂","③").replace("➃", "④").replace("➄", "⑤")
			行 = 行.lstrip("q")
			行 = 行.replace("-", "(新派錯音)")
		elif 名 in ("南京"):
			行 = re.sub("([，。])(（)", "\\2\\1", 行)
			行 = 行.replace("，", "（又）").replace("。", "（新）").replace("）（", " ")
			行 = 自.normS(行, "{\\1}")
			行 = re.sub(r"(\{[^{}]+?)（又）([^{}]*?\})", "\\1，\\2", 行)
			行 = re.sub(r"(\{[^{}]+?)（新）([^{}]*?\})", "\\1。\\2", 行)
		elif 名 in ("常熟古裡",):
			行 = re.sub(r"\{[^{}]*?[①-⑨][^{}]*?\}", 常熟古裡_repl, 行)
		elif 名 in ("句容",):
			if re.match(".*[①-⑨ⓐⓑ]+", 行):
				for i in range(1,10):
					sda = chr(ord('①') + (i - 1))
					sdb = f"［{i}］"
					行 = 行.replace(sda, sdb)
			行 = 行.replace("］ⓐ", "a］").replace("］ⓑ", "b］")
		elif 名 in ("休寧",):
			行 = 行.replace("[3ˀ]", "[3]")
		elif 名 in ("光澤寨裏",):
			行 = 行.replace("‖", "")
		elif 名 in ("泰州",):
			行 = 行.replace("'", "ʰ")
			行 = re.sub("([-=])(.)", "\\2\\1", 行)
		elif 名 in ("吉水金灘", "繁昌"):
			行 = re.sub("([mnvʋɹl])([\u0329\u030D]+)", "\\1\u0329", 行)
			行 = re.sub("([ŋȵʐɱɻʒ])([\u0329\u030D]+)", "\\1\u030D", 行)
		elif 名 in ("贛楡", "徐州", "銀川", "大同", "儀徵"):
			行 = 行.strip().replace(",","，").replace(";","；").replace(":","：").replace("？（", "□（")
			行 = 行.lstrip("øq")
			if 行.startswith("#"): return 行
			行 = re.sub(r"([？#\-\+])(.)", "\\2\\1", 行)
			行 = 行.replace("-", "(舊)").replace("+", "/").replace("？", "?").replace("#", "*")
		elif 名 in ("黨項",):
			行 = re.sub(r"(.\{)", "[0]\\1", 行, count=1)
		return 行

	def 析韻(自, 行):
		韻 = None
		行 = 行.strip()
		if not 行: return 韻
		if 行.startswith("#"): 行 = 行[1:]
		elif "［" in 行 or "］" in 行: return 韻
		韻 = 行
		if 韻:
			韻 = 韻.split("\t")[0].strip().strip("[]")
		自.韻 = 韻
		return 韻
	
	def 更新(自):
		典 = defaultdict(list)
		韻 = ""
		skip = 自.info.get("跳過行數", 0)
		lineno = 0
		for 行 in open(自.spath,encoding="U8"):
			lineno += 1
			if lineno <= skip: continue
			行 = 自.統(行)
			if not 行: continue
			行 = 行.strip().replace("＝","=").replace("－", "-").replace("—","-").replace("｛","{").replace("｝","}").replace("?","？").replace("：[", "	[").replace("{：",'{')
			行 = 行.replace("[·]", "[0]")
			行 = re.sub(r"\[(\d+[a-zA-Z]?)\]", "［\\1］",行)
			行 = re.sub("［([^0-9]+.*?)］", "[\\1]",行)
			if ("{" not in 行 and "｛" not in 行) and ("（" in 行 or "(" in 行):
				行 = 自.normS(行, "{\\1}")
			行 = 行.lstrip(" ")
			if "［" not in 行 and re.match(".*[⓪①-⑨]", 行):
				for i in range(1, 10):
					sda = chr(ord('①') + (i - 1))
					sdb = f"［{i}］"
					行 = 行.replace(sda, sdb)
				行 = 行.replace("⓪", "［0］")
			if (s := 自.析韻(行)) is not None:
				韻 = s
				continue
			matches = re.findall("^([^［］]*?)(［.+)$", 行)
			if not matches or len(matches[0]) != 2: continue
			列 = list(matches[0])
			列[0] = 列[0].strip().strip("[]")
			列[1] = 列[1].replace("\t", "")
			列[1] = re.sub(r" (\d)", "\\1", 列[1])
			聲, 字組 = 列
			for sd,字組 in re.findall(r"［(\d+[a-zA-Z]?)］([^［］]+)", 列[1]):
				音 = 聲 + 韻 + sd
				音 = 自.checkYb(音)
				字組 = 自.normG(字組)
				字組 = re.findall(r"(.)([\d₀-₉]?)([<+\-/=\\\*？$&r@]?)[\d₀-₉]? *(｛.*?｝)?", 字組)
				for 字, o, c, js in 字組:
					if 字 == " ": continue
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
								p = "(爲舊)"
								c = ""
					js = js[1:-1]
					if js.count("{") != js.count("}"):
						自.錯誤.append(f"大括號未成對:{js}")
						js = js.replace("{", "").replace("}", "")
					if o and ("₀" <= o <= "₉"):
						o = chr(ord(o) - ord("₀") + ord("0"))
					p = 音 + c + "\t" + o + p + js
					if p not in 典[字]:
						典[字].append(p)
		自.寫(典)
