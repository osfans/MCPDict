#!/usr/bin/env python3

import re
from collections import defaultdict
from tables._表 import 表 as _表
from tables.__init__ import 有字

class 表(_表):
	註序 = True
	聲 = ""
	韻 = ""
	韻組 = list()
	韻乙 = ""

	def 統(自, 行):
		行 = _表.統(自, 行)
		名 = 自.簡稱
		if 名 in ("黃梅小池","光山南郊", "信陽東雙河"):
			行 = 自.normM(行)
		elif 名 in ("巢湖",):
			行 = 自.normS(行)
		elif 名 in ("奉化",):
			行 = re.sub(r"(\d+)(?![:\d])", "[\\1]", 行)
		elif 名 in ("美斯樂果敢話", "美斯樂瀾滄話", "美斯樂騰衝話", "北京", "德安", "興國", "興國茶園", "永新", "安遠", "廉州", "賀州鋪門", "賀州靈鳳", "賀州擔石", "梧州倒水", "柳城古砦", "融安", "孝昌小河", "永福桃城", "瑞金", "上杭藍溪", "平樂船上話", "富川秀水", "松江"):
			上標 = "⁰¹²³⁴⁵⁶⁷⁸⁹"
			行 = re.sub(r"([⁰¹²³⁴⁵⁶⁷⁸⁹]+)", lambda x:"".join([str(上標.index(i)) for i in x.group(1)]), 行)
			行 = re.sub(r"\{(\d+)\}", "\\1", 行)
			行 = re.sub(r"(\d+[ab]?)", "[\\1]", 行, 1)
			if "[" not in 行: 行 = ""
		elif 名 in ("羅山","贛縣安平"):
			行 = re.sub(r"[:] ?\[", "	[", 行)
		elif 名 in ("介休張蘭",):
			行 = re.sub(r"\[(\d)\]\)","\\1)",行)
		elif 名 in ("江山廿八都",):
			行 = re.sub("([&@])(?!{)","{\\1}",行)
			行 = 行.replace("&{","{&").replace("@{","{@")
		elif 名 in ("樅陽雨壇","潛山","青陽客籍話"):
			行 = 行.replace("*", "□")
		elif 名 in ("南雄珠璣巷"):
			行 = re.sub(r"(\d+)", "[\\1]", 行, count=1)
			if not 行.startswith("#") and "[" not in 行: 行 = ""
		elif 名 in ("樅陽東",):
			行 = 行.replace("*", "□")
			行 = 自.normS(行)
			行 = re.sub("[가-힣]+[, ]*", "", 行).lstrip()
			if 行.startswith("#"):
				行 = re.sub('^(#[^ ]*) .*?	', '\\1', 行)
			elif "[" in 行:
				行 = re.sub(r'(.*?)[/ ].*?	(\[.+)$', '\\1	\\2', 行)
			else:
				行 = ""
		elif 名 in ("臨高話",):
			if " " not in 行: return "#"
			行 = 行.strip()
			行 = re.sub(r"<(.*?)>","\\1{讀書音聲調}",行)
			行 = re.sub(r"\[(.*?)\]","\\1{特殊音}",行)
			行 = re.sub(r"(.)\*","\\1{海口話影響}",行)
			行 = re.sub(r"( [1-5])", " [\\1]", 行)
			行 = re.sub(r"([ptk]) ", "\\1 [5]", 行)
			行 = re.sub(r"^(.*?)\[", "\\1	[", 行)
			行 = 行.replace(" ", "")
		elif 名 in ("瀘溪李家田"):
			行 = re.sub(r"(\[1\])([^\[‖\]]*?)(‖)", "\\1\\2[1x]",行)
			行 = re.sub(r"(\[2\])([^\[‖\]]*?)(‖)", "\\1\\2[5x]",行)
			行 = re.sub(r"(\[3\])([^\[‖\]]*?)(‖)", "\\1\\2[6x]",行)
			行 = re.sub(r"(\[5\])([^\[‖\]]*?)(‖)", "\\1\\2[2x]",行)
			行 = re.sub(r"(\[7\])([^\[‖\]]*?)(‖)", "\\1\\2[3x]",行)
		elif 名 in ("贛州",):
			行 = 行.replace("&{", "{&").replace("&", "(連讀調)").replace("${", "{$").replace("$", "(單字調)")
		elif 名 in ("浦城觀前","泰順莒江","龍游靈上","南平洋頭", "蓮花坊樓","延安老户話","慶元江根","閩清坂東", "泰順三魁", "蒼南炎亭", "玉山", "常山", "慶元", "廣豐", "江山"):
			行 = re.sub(r"(‖)(\[\d+\])", "\\2\\1",行)
			while (newline := re.sub(r"(?<=‖)([^\[\]]*[^‖]){", "\\1‖{", 行)) != 行:
				行 = newline
			行 = re.sub("‖{", "{(連讀音)", 行).replace("‖", "")
		elif 名 in ("福鼎白琳","福清","福安穆陽", "屏南","古田大橋","古田杉洋", "霞浦長春", "柘榮富溪", "壽寧斜灘"):
			行 = re.sub(r"([\|‖])(\[\d+\])", "\\2\\1",行)
			while (newline := re.sub(r"(?<=‖)([^\[\]]*[^‖]){", "\\1‖{", 行)) != 行:
				行 = newline
			while (newline := re.sub(r"(?<=\|)([^\[\]]*[^\|]){", "\\1|{", 行)) != 行:
				行 = newline
			行 = re.sub(r"\|{", "{(連讀音Ⅰ)", 行).replace("|", "")
			行 = re.sub("‖{", "{(連讀音Ⅱ)", 行).replace("‖", "")
		elif 名 in ("建德"):
			行 = re.sub(r"\t2\d.*$", "", 行)
		elif 名 in ("屯溪船上話"):
			行 = re.sub(r"连读.*$", "", 行)
		elif 名 in ("潼關太要",):
			if 行.startswith("["): 行 = ""
		elif 名 in ("昆明",):
			if 行.startswith("\t\t"): 行 = ""
			行 = re.sub(r"^.*?\t", "", 行)
			行 = 行.replace("(", "{").replace("〔", "{").replace(")", "}")
		elif 名 in ("建水",):
			if 行.startswith("\t"): 行 = ""
		elif 名 in ("丹鳳","嘉定中","嘉定西","嘉定城","嘉定外","寶山","寶山羅店","南皮","海門","長治"):
			if 行.startswith("#"): 行 = "#"
		elif 名 in ("商州",):
			if 行.startswith("#"): 行 = "#"
			行 = re.sub(r"\[([^\d]+)\]", "\\1", 行)
		elif 名 in ("永定", "連城四堡", "上杭古田"):
			行 = 行.replace("*", "@")
		elif 名 in ("通道菁蕪洲",):
			行 = re.sub("([&])(?!{)","{西官借詞}",行).replace("&{","{(西官借詞)")
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
			行 = 行.replace("*(", "□(")
			行 = 自.normS(行)
			行 = re.sub(r"\*(.)", "\\1?", 行)
			行 = re.sub(r"\[(.)(.*?)\]", "\\1*\\2", 行)
			列 = 行.split("\t")
			for i,調 in enumerate(自.調典.values()):
				if 列[i + 1]:
					列[i + 1] = f"[{調}]" + 列[i + 1]
			行 = "".join(列)
		elif 名 in ("虔南大吉山",):
			行 = re.sub(r"(\[)(.*?)(\d+\])", "\\1\\3", 行)
			行 = 行.replace("<","{").replace(">","}")
		elif 名 in ("慈利",):
			行 = 行.replace("/", "")
		elif 名 in ("東莞塘角"):
			if 行.startswith("#"): return "#"
			果 = re.findall(r"\[(.*?)(\d+)[ab]?\]", 行)
			if not 果: return
			聲韻 = 果[0][0]
			行 = 聲韻 + 行.replace("["+聲韻, "[")
		elif 名 in ("東干甘肅話",):
			if 行.startswith("#"):
				韻組 = 行.rstrip().replace("#", "").split("\t")
				if len(韻組) != 2: return
				韻, 韻乙 = 韻組
				自.韻乙 = 韻乙
				return f"#{韻}"
			聲, 聲乙, 字組 = 行.split("\t", 2)
			聲 = f"{聲乙}{自.韻乙}/{聲}".replace("Ø", "")
			return f"{聲}\t{字組}"
		elif 名 in ("敦煌", "洛陽"):
			行 = 行.rstrip("12345 \t\n")
			行 = re.sub(r"\[([^\d].*?)\]", "(\\1)", 行)
			行 = 自.normS(行)
		elif 名 in ("梧州",):
			if 行.startswith("#"): return
			行 = re.sub(r"\[(.*?)(\d+)\]", "\n\\1[\\2]", 行)
		elif 名 in ("博白"):
			if 行.startswith("#"): return "#"
			果 = re.findall(r"\[(.*?)(\d+)\]", 行)
			if not 果: return
			聲韻 = 果[0][0]
			行 = re.sub(r"\[(.*?)(\d+)\]", "[\\2]", 行)
			行 = 聲韻 + 行
		elif 名 in ("博羅",):
			if "[" not in 行 and not 行.startswith("#"): 行 = ""
		elif 名 in ("金壇",):
			if 行.strip().endswith("韻"): 行 = ""
		elif 名 in ("成都", "響水"):
			行 = 行.strip()
			if 行.startswith("["):
				行 = 自.聲 + 行
			else:
				自.聲 = 行.split("[")[0]
		elif 名 in ("威海",):
			行 = 行.replace("/", "", 2)
		elif 名 in ("烏魯木齊","西寧","蒙山新圩","青島","天台東鄉","景寧東坑"):
			行 = re.sub(r"(\d+)", "[\\1]", 行, count=1).strip()
			if 行.startswith("["):
				行 = 自.聲 + 行
			else:
				自.聲 = 行.split("[")[0]
		elif 名 in ("天台城關"):
			行 = re.sub(r"(\d)", "[\\1]", 行)
			行 = re.sub(r"^(.*?)(\[)", "\\1	\\2", 行)
			行 = 自.normS(行)
			if "[" not in 行: 行 = ""
		elif 名 in ("高郵"):
			行 = 行.replace("-", "(新派錯音)")
		elif 名 in ("南京"):
			行 = re.sub(r"([，。])(\()", "\\2\\1", 行)
			行 = 行.replace("，", "(又)").replace("。", "(新)").replace(")(", " ")
			行 = 自.normS(行)
			行 = re.sub(r"(\{[^{}]+?)\(又\)([^{}]*?\})", "\\1，\\2", 行)
			行 = re.sub(r"(\{[^{}]+?)\(新\)([^{}]*?\})", "\\1。\\2", 行)
		elif 名 in ("句容", "蘭谿諸葛"):
			if re.match(".*[⓪①-⑨ⓐⓑ]+", 行):
				for i in range(0,10):
					sda = "⓪" if i == 0 else chr(ord('①') + (i - 1))
					sdb = f"[{i}]"
					行 = 行.replace(sda, sdb)
			行 = 行.replace("]ⓐ", "a]").replace("]ⓑ", "b]")
		elif 名 in ("休寧",):
			行 = 行.replace("[3ˀ]", "[3]")
		elif 名 in ("光澤寨裏",):
			行 = 行.replace("‖", "")
		elif 名 in ("1935醴陵",):
			列 = 行.split("\t", 1)
			列[1] = 列[1].replace("/", "")
			行 = "\t".join(列)
		elif 名 in ("南海沙頭"):
			行 = re.sub(r"^(\d+)(\()", "\\1□\\2", 行)
			行 = re.sub(r"^(\d+)", "[\\1]", 行)
		elif 名 in ("筠連","瀘縣福集"):
			列 = 行.split("\t")
			if 列[0] == "" and 列[1].startswith("阴平"):
				自.調值表 = 列
				return
			elif 列[0] == "":
				行 = 列[1]
			else:
				行 = "\t".join((f"[{自.調值表[序][-2:]}]" if 序 else "") + 項 for 序,項 in enumerate(列) if 項)
		elif 名 in ("大埔百候",):
			列 = 行.split("\t")
			if 列[0] == "#a":
				自.調值表 = 列
				行 = 列[0]
			elif 列[0].startswith("#"):
				行 = 列[0]
			else:
				行 = "\t".join((f"[{自.調值表[序]}]" if 序 else "") + 項 for 序,項 in enumerate(列) if 項)
		elif 名 in ("衡山望峰",):
			列 = 行.split("\t")
			if 有字(列[0]):
				if len(列) == 2:
					行 = "#" + 列[1]
				else:
					自.調值表 = [re.findall(r"^([^\d]+)(\d*)$", i)[0][1] for i in 列]
					return
			else:
				行 = "\t".join((f"[{自.調值表[序]}]" if 序 else "") + 項 for 序,項 in enumerate(列) if 項)
		elif 名 in ("自貢","漢源","達州","峨眉"):
			if not 行.startswith("#"):
				列 = 行.split("\t")
				if not 列[0] or 有字(列[0]): return
				行 = "\t".join((f"[{(序 + 2 if (序 >= 5) else 序 + 1) if (序 >= 4)  else 序}]" if 序 else "") + 項 for 序,項 in enumerate(列))
		elif 名 in ("江永夏層舖", "江永回龍圩", "江永粗石江", "江永蘭溪", "江永允山"):
			if not 行.startswith("#"):
				列 = 行.split("\t")
				if 有字(列[0]): return
				行 = "\t".join((f"[{序 + 1 if ("江永粗石江" == 名 and 序 > 3) or ("江永蘭溪" == 名 and 序 > 5) else 序}]" if 序 else "") + 項 for 序,項 in enumerate(列))
				行 = 行.replace("（", "(").replace("）", ")").replace("(", "{").replace(")", "}").replace("{{", "{").replace("}}", "}")
		elif 名 in ("岳陽張谷英",):
			列 = 行.split("\t")
			if 列[0] == "音节":
				列 = 行.rstrip().split("\t")
				if len(列) == 2:
					return 列[1]
				自.調值表 = [i[-2:] for i in 列]
				return
			行 = "\t".join((f"[{自.調值表[序]}]" if 序 and 項 else "") + 項 for 序,項 in enumerate(列))
		elif 名 in ("南通",):
			列 = 行.split("\t")
			if 列[0] == "文=读= 白-读-":
				列 = (行.rstrip()+"0").split("\t")
				列 = [re.sub(r"^(.*?)(\d*)$", "\\2", i) for i in 列]
				自.調值表 = 列
				return
			行 = "\t".join((f"[{自.調值表[序]}]" if 序 and 項 else "") + 項 for 序,項 in enumerate(列))
			行 = 行.replace("【", "{").replace("】", "}")
			if "蛆" in 行: print(行)
		elif 名 in ("通州五接","如皋白蒲","如皋石莊","南通唐閘",):
			列 = 行.split("\t")
			if 列[0] == "":
				列 = 行.rstrip().split("\t")
				if 列.count(列[-2]) == 2: 列[-2] += "0"
				if 列.count(列[-1]) == 2: 列[-1] += "0"
				自.調值表 = 列
				自.調值表.append("0")
				return
			行 = "\t".join((f"[{自.調值表[序]}]" if 序 and 項 else "") + 項 for 序,項 in enumerate(列))
			行 = 自.normS(行.replace(")(", "："))
		elif 名 in ("葛洲壩",):
			if 行.startswith("["): return
			列 = 行.split("\t")
			if len(列) <= 1: return 行
			行 = "\t".join((f"[{序 + 1 if 序 > 3 else 序}]" if 序 and 項 else "") + 項 for 序,項 in enumerate(列))
		elif 名 in ("仙遊蓋尾",):
			if "[" not in 行:
				行 = 行.replace("-", "").strip()
		elif 名 in ("臨江",):
			if "[" in 行:
				列 = 行.split("\t")
				行 = 列[0] + 列[2] + 列[1]
		elif 名 in ("安澤英寨"):
			if 行:
				列 = 行.split("\t")
				if len(列) <= 1:
					return
				起 = 列[0]
				if 有字(起):
					if 起 == "韵调声" and len(列) == 2:
						行 = 列[1]
						自.韻乙 = ""
					elif 起 == "韵声":
						自.韻甲, 自.韻乙 = 列[1:]
						return
					else:
						return
				else:
					if 自.韻乙:
						行 = 自.韻甲
						行 += "\n" + 列[0] + "\t" + "[7]" + 列[1]
						if len(列) == 3:
							行 += "\n" + 自.韻乙
							行 += "\n" + 列[0] + "\t" + "[7]" + 列[2]
					else:
						行 = "\t".join((f"[{序 if 序 <= 3 else 序 + 1}]" if 序 else "") + 項 for 序,項 in enumerate(列))
					行 = 行.replace("（", "(").replace("）", ")").replace("(", "{").replace(")", "}").replace("{{", "{").replace("}}", "}").replace("ø", "")
		elif 名 in ("左雲"):
			列 = 行.split("\t")
			列數 = len(列)
			if 有字(列[0]): return
			if 列數 == 2 and 列[1] == "入声4":
				return 列[0]
			行 = "\t".join((f"[{7 if 列數 == 2 and 序 == 1 else (5 if 序 == 4 else 序)}]" if 序 else "") + 項 for 序,項 in enumerate(列))
			行 = 行.replace("（", "(").replace("）", ")").replace("(", "{").replace(")", "}").replace("{{", "{").replace("}}", "}")
		elif 名 in ("贛榆", "徐州", "銀川", "大同", "儀徵"):
			行 = 行.strip().replace(",","，").replace("?(", "□(")
			if 行.startswith("#"): return 行
			行 = re.sub(r"([\?#\-\+])([^)])", "\\2\\1", 行)
			行 = 行.replace("-", "(舊)").replace("+", "/").replace("#", "*")
		elif 名 in ("檳城閩南話",):
			if 行.startswith("#"): pass
			elif 行.startswith("*"): 行 = 行.replace("*", "#")
			elif "[" not in 行: 行 = ""
		elif 名 in ("党項",):
			行 = re.sub(r"(.\{)", "[0]\\1", 行, count=1)
		elif 名 in ("淮劇",):
			行 = re.sub(r"1(?!\])", "(建湖音)", 行)
			行 = re.sub(r"2(?!\])", "(阜寧音)", 行)
			行 = re.sub(r"3(?!\])", "(官話音)", 行)
			行 = 行.replace(")(", ",")
		return 行

	def 析韻(自, 行):
		行 = 行.strip()
		if not 行: return
		if 行.startswith("#") or 行.startswith("＃"): 行 = 行[1:]
		elif "［" in 行 or "］" in 行: return
		韻 = 行
		if 韻: 韻 = 韻.split("\t")[0].strip().strip("[]")
		if 有字(韻):
			if 自.韻: 自.誤.append(f"[{韻}]前不應斷行，或不是合法韻母")
			return 自.韻
		自.韻 = 韻
		if 韻: 自.韻組.append(韻)
		return 韻

	def 更新(自):
		典 = defaultdict(list)
		韻 = ""
		跳過行數 = 自.info.get("跳過行數", 0)
		字表使用調值 = 自.info.get("字表使用調值", False)
		行號 = 0
		for 行 in open(自.spath,encoding="U8"):
			行號 += 1
			if 行號 <= 跳過行數: continue
			統行 = 自.統(行)
			if not 統行: continue
			for 行 in 統行.split("\n"):
				if 字表使用調值:
					行 = re.sub(r"\[([\d-]+)\]", lambda x:"[%s]"%自.僅轉調類(x[1], 韻), 行)
				行 = 行.strip().replace(":[", "	[").replace("{:",'{')
				行 = 行.replace("[·]", "[0]")
				行 = re.sub(r"\[(\d+[a-zA-Z]?)\]", "［\\1］",行)
				行 = re.sub(r"［([^\d]+.*?)］", "[\\1]",行)
				if ("｛" not in 行 and "{" not in 行) and ("(" in 行):
					行 = 自.normS(行)
				行 = 行.lstrip(" ")
				if "［" not in 行 and re.match(".*[⓪①-⑨]", 行):
					for i in range(0, 10):
						sda = "⓪" if i == 0 else chr(ord('①') + (i - 1))
						sdb = f"［{i}］"
						行 = 行.replace(sda, sdb)
				if (s := 自.析韻(行)) is not None:
					韻 = s
					continue
				果 = re.findall("^([^［］]*?)(［.+)$", 行)
				if not 果 or len(果[0]) != 2: continue
				列 = list(果[0])
				列[0] = 列[0].strip().strip("[]")
				列[1] = 列[1].replace("\t", "")
				列[1] = re.sub(r" (\d)", "\\1", 列[1])
				聲, 字組 = 列
				if 韻 != "" and 聲 == "ø": 聲 = ""
				for 調,字組 in re.findall(r"［(\d+[a-zA-Z]?)］([^［］]+)", 列[1]):
					音 = 自.正音(聲 + 韻 + 調, True)
					if "/" in 音 and 自.簡稱 != "東干甘肅話":
						音 = []
						for i in 聲.split("/"):
							for j in 韻.split("/"):
								k = 自.正音(i + j + 調, True)
								if k not in 音: 音.append(k)
						音 = "/".join(音)
					字組 = 自.normG(字組)
					字組 = re.findall(r"(.)(\d?)([<+\-/=\\\*\?$&r@]?)\d? *(｛.*?｝)?", 字組)
					for 字, 序號, 異讀, 註 in 字組:
						if 字 == " ": continue
						音義 = ""
						if 異讀:
							if 異讀 in "+-*/=@\\":
								pass
							else:
								if 異讀 == '?':
									音義 = ""
								elif 異讀 == 'r':
									音義 = "(兒化)"
									異讀 = ""
						註 = 註[1:-1]
						if 註.count("{") != 註.count("}"):
							自.誤.append(f"括號未成對:{註}")
							註 = 註.replace("{", "").replace("}", "")
						音義 = 音 + 異讀 + "\t" + 序號 + 音義 + 註
						if 音義 not in 典[字]:
							典[字].append(音義)
						if 音 not in 自.音表: 自.音表[音] = list()
						自.音表[音].append(字)
		自.寫(典)
