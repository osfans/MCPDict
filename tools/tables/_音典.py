#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables.__init__ import 找字
import re
from pypinyin.contrib.tone_convert import to_tone3

class 表(_表):
	def 析(自, 列):
		名 = 自.簡稱
		字 = ""
		音 = ""
		音組 = []
		註 = ""
		if 自.列序:
			列序 = 自.列序
			if len(列) <= 列序[0]: return
			字 = 列[列序[0]]
			if (len(列序) == 5 or len(列序) == 3) and 0 <= 列序[-1] < len(列):
				註 = 列[列序[-1]].strip("{}")
			if len(列序) == 2 or len(列序) == 3 or 列序[1] == 列序[2] == 列序[3]:
				if 列序[1] < len(列): 音= 列[列序[1]]
			elif 列序[1] == 列序[2] != 列序[3]:
				音= 列[列序[1]] + 列[列序[3]]
			elif 列序[1] != 列序[2] and 列序[3] < 0:
				音= 列[列序[1]] + 列[列序[2]]
			elif 列序[1] != 列序[2] != 列序[3]:
				if len(列) <= 列序[3]:
					print(名, 列, 列序)
				音= 列[列序[1]] + 列[列序[2]] + 列[列序[3]]
			if 字.endswith("-") or 字.endswith("="):
				音 += 字[-1]
				字 = 字[:-1]
			if not 字 or not 音: return
			if 名 in ("信宜新寶",):
				if 字 == 註: 註 = ""
			elif 名 in ("國語",):
				音 = to_tone3(音)
			elif 名 in ("蒼南錢庫",):
				音 = 音.replace("轻声", "0")
			elif 自.文件名.startswith("寧夏中衛八点联表"):
				註 = 字[1:].strip("()（）")
				字 = 字[0]
			elif 名 in ("雷州",):
				音 = 音.replace("˨˨˩", "˨˩")
			elif 名 in ("東莞東坑",):
				音 = re.sub(r"^(.*?)(\d+)/(\d+)$", r"\1\2/\1\3", 音)
			elif 名 in ("開化",):
				if re.match(r"（.*?）", 註): 註 = 註[1:-1]
			elif 名 in ("江門",):
				原註 = 列[11]
				if 原註: 註 = 原註 + "。" + 註
				註 = 註.strip("。")
			elif 名 in ("南寧", "南寧亭子"):
				音 = 音.replace(" ", "-")
				註 = 列[6] + 註
			elif 名 in ("陽春潭水",):
				音 = 音.replace("或", "/")
			elif 名 in ("鄭張上古",):
				音 = ("%s%s (%s%s切 %s聲 %s%s)"%(列[12], f"/{列[13]}" if 列[13] else "", 列[7],列[8],列[9],列[10],列[11]))
			elif 名 in ("溫州",):
				toneValues = {'阳入':8,'阴上':3,'阳平':2,'阴入':7,'阳去':6,'阴平':1,'阴去':5,'阳上':4}
				if 音[-2:] in toneValues:
					音 = 音[:-2] + str(toneValues[音[-2:]])
			elif 名 in ("瑞安陶山",):
				備註 = 列[5]
				註 = (註 + " " +備註).strip()
			elif 名 in ("詔安白葉","詔安霞葛"):
				if " " in 音:
					l = list()
					for y,j in zip(音.split(" "), 註.split(" ")):
						l.append((字, y, j))
					return l
			elif 名 in ("普寧占隴西",):
				l = list()
				for z in 字:
					l.append((z,音,註))
				return l
			elif 名 in ("隆都沙溪話",):
				if " " in 音:
					l = list()
					備註 = 註
					if 列[3]: 註 = 列[3]
					for y,j in zip(音.split(" "), 註.split(" ")):
						l.append((字, y,(備註.lstrip(y+"為") if 備註.startswith(y) else "") + j.lstrip(y)))
					return l
			elif 自.文件名.startswith("丹陽（雲陽訪仙河陽埤城）"):
				註 = 字[1:].strip("()（）")
				字 = 字[0]
				if 字 == "[": return
				音標 = 音
				if "、" in 音標:
					音標組 = 音標.split("、")
					l = list()
					for 音標 in 音標組:
						音標, 音註 = re.findall(r"^(.*\d+)([^\d]*?)$", 音標)[0]
						音 = 自.轉調類(音標)
						l.append((字, 音, 音註 if 音註 else 註))
					return l
			elif 自.文件名.startswith("洪洞方言语音比较研究"):
				音標 = 音
				if "/" in 音標:
					音標組 = 音標.split("/")
					l = list()
					for 音標 in 音標組:
						音標, 音註 = re.findall(r"^(.*\d+[-=]?)([^\d]*?)$", 音標)[0]
						音 = 自.轉調類(音標)
						l.append((字, 音, 音註 if 音註 else 註))
					return l
			elif 自.文件名.startswith("东莞20") or 自.文件名.startswith("東莞語料合輯"):
				訓 = 音.startswith("(")
				音標 = 音.strip("()")
				for 音 in 音標.split("|"):
					if 訓: 音 += "@"
					音組.append(音)
			elif 自.文件名.startswith("白語_袁明軍"):
				if 註 == 字: 註 = ""
				上標 = "⁰¹²³⁴⁵⁶⁷⁸⁹"
				for i in 上標:
					音 = 音.replace(i, str(上標.index(i)))
			elif 自.文件名.startswith("贵州六盘水八点联表") or 自.文件名.startswith("永州南部土話聯表") or 自.文件名.startswith("广元剑阁5点联表") or 自.文件名.startswith("自贡富顺4点联表"):
				註 = 字[1:].strip("()（）")
				字 = 字[0]
				轉調類 = 自.info.get("字表使用調值", False)
				if "/" in 音:
					音組 = 音.split("/")
					l = list()
					for 項 in 音組:
						if "(" in 項:
							項, 註 = 項.split("(", 1)
							註 = 註[:-1]
						else:
							註 = 列[0][1:].strip("()（）")
						if 轉調類: 項 = 自.轉調類(項)
						l.append((字, 項, 註))
					return l
			elif 自.文件名.startswith("粤西闽语方言字表"):
				if len(列) < 6: return
				字 = 字.strip("()")
				音集 = 音
				if 音集.startswith("(") and 音集.endswith(")"): 音集 = 音集[1:-1]
				if not 音集 or 音集.startswith("—"): return
				_js = 字[1:] if len(字)>1 else ""
				_js = _js.strip("（）")
				字 = 字[0]
				l = list()
				for 音標 in 音集.split("/"):
					音標 = 音標.strip()
					c = ""
					if "(" in 音標:
						n = 音標.index("(")
						c = 音標[n:]
						音標 = 音標[:n]
					音 = 自.轉調類(音標)
					註 = c + _js
					if 註.startswith("(") and 註.endswith(")"):
						註 = 註[1:-1]
					l.append((字, 音, 註))
				return l
			elif 自.文件名.startswith("闽西清流，宁化客家话比较研究"):
				if 音 == "─": return
				if ";" in 音:
					音標組 = 音.split(";")
					l = list()
					for 音標 in 音標組:
						if re.match("^.*[¹²³⁴⁵]+$", 音標):
							l.append((字, 自.轉調類(音標), 註))
							continue
						音標, 註2 = re.findall("^(.*?[¹²³⁴⁵]+)(.*?)$", 音標)[0]
						l.append((字, 自.轉調類(音標), 註2))
					return l
			elif 自.文件名.startswith("湖南洞绥片赣方言语音调查研究"):
				if 自.無音(音): return
				if "/" in 音:
					l = list()
					音 = re.sub(r"^(.*?)(\d+)/(\d+)$", r"\1\2/\1\3", 音)
					for i, 音標 in enumerate(音.split("/")):
						l.append((字, 自.轉調類(音標) + ("-" if i == 1 else "="), 註))
					return l
			elif 自.文件名.startswith("晋陕蒙交界地区晋方言语音研究"):
				markers = list(map(chr, range(0xa700,0xa708)))
				l = list()
				異讀 = "/" in 音
				n = len(音.split("/"))
				for i,音標 in enumerate(re.split(r"[/\|]", 音)):
					if not 音標: continue
					if 音標[0] in markers: 音標 = 音標[1:] + str(markers.index(音標[0]) + 1)
					elif 音標[-1] in markers: 音標 = 音標[:-1] + str(markers.index(音標[-1]) + 1)
					if 異讀:
						音標 = 音標 + ("=" if i == n - 1 else "-")
					l.append((字,音標,註))
				return l
			elif 自.文件名.startswith("陇东方言语音研究"):
				markers = list(map(chr, range(0xa700,0xa708)))
				l = list()
				異讀 = "/" in 音
				for i,音標 in enumerate(音.split("/")):
					n = 找字(音標)
					本註 = 音標[n:]
					音標 = 音標[:n]
					if not 音標: continue
					if not 本註: 本註 = 註
					if 音標[0] in markers: 音標 = 音標[1:] + str(markers.index(音標[0]) + 1)
					elif 音標[-1] in markers: 音標 = 音標[:-1] + str(markers.index(音標[-1]) + 1)
					if 異讀:
						音標 = 音標 + ("-" if i == 1 else "=")
					l.append((字,音標,本註))
				return l
			elif 自.文件名.startswith("语言接触与湘西南苗瑶平话调查研究"):
				if 自.無音(音): return
				if ";" in 音:
					音標組 = 音.split(";")
					l = list()
					for 音標 in 音標組:
						音標 = 自.正音(音標)
						if re.match(r"^.*[¹²³⁴⁵]+[\-=]?$", 音標):
							l.append((字, 自.轉調類(音標), 註))
							continue
						音標, 註2 = re.findall(r"^(.*?[¹²³⁴⁵]+[\-=]?)(.*?)$", 音標)[0]
						l.append((字, 自.轉調類(音標), 註2))
					return l
			elif 自.文件名.startswith("安徽淮河流域方言语音比较研究"):
				markers = list(map(chr, range(0xa700,0xa708)))
				l = list()
				for i in 音.split("/"):
					if i[0] in markers: i = i[1:] + str(markers.index(i[0]) + 1)
					elif i[-1] in markers: i = i[:-1] + str(markers.index(i[-1]) + 1)
					l.append((字,i,註))
				return l
			elif 自.文件名.startswith("广西富川富阳方言21点"):
				註 = 字[1:].strip("()（）")
				字 = 字[0]
				if "/" in 音:
					音標組 = 音.split("/")
					l = list()
					l.append((字, 自.轉調類(音標組[0]), 註))
					if len(音標組) > 1:
						if "(" in 音標組[1]:
							音標, 註 = re.findall(r"([^()]*)\((.*)\)", 音標組[1])[0]
						else:
							音標 = 音標組[1]
						l.append((字, 自.轉調類(音標), 註))
					return l
			elif 名 in ("鶴山沙坪",):
				韻, 調值 = 列[10], 列[11]
				for i in 韻.split("，"):
					音組.append(音 + i + 調值)
			elif 名 in ("1925鹽城"):
				l = list()
				for 項 in 字.split(" "):
					if len(項) == 2:
						註 = f"（{項}）{註}".strip()
						項 = 項[0]
					elif len(項) > 2:
						註 = f"{項[1:]}{註}".strip()
						項 = 項[0]
					l.append((項, 音, 註))
				return l
			elif 名 in ("榮縣",):
				音 = 音.lstrip("Øø")
				if "或" in 音:
					音 = re.sub(r"(.*?)(\d+)或(\d+)", r"\1\2/\1\3", 音)
			elif 名 in ("昭平龍坪",):
				調名 = ("陰平","陽平","陰上","陽上","陰去","陽去","上","中","下")
				調值 = sorted(自.調典.values())
				調表 = dict(zip(調名, 調值))
				音 = re.sub("([陰陽平上去入中下]+)(\\d*?)$", lambda x:調表.get(x.group(1)), 音)
		elif 自.文件名.startswith("榕江侗"):
			列[0] = 列[0].strip().replace(" /", "/").replace(" [", "[")
			if not 列[0]: return
			果 = 列[0].split(" ", 2)
			if len(果) < 2: return
			if len(果) == 2:
				字, 音 = 果
				註 = ""
			else:
				字, 音, 註 = 果
				註 = 註.strip("{}")
			if "/" in 音:
				l = list()
				音組 = 音.split("/")
		elif len(列) == 2:
			字, 音 = 列[:2]
		elif len(列) >= 3:
			字, 音, 註 = 列[:3]
		if len(字) != 1 or not 音: return
		if 名 in ("白－沙上古"):
			音乙 = 音.split(" ", 1)
			音乙[0] = 音乙[0].lstrip("*").replace(".", "．").replace("-", "－").replace("(", "（").replace(")", "）").replace("[", "［").replace("]", "］").replace("<", "〈").replace(">", "〉")
			音 = " ".join(音乙)
		if 名 in ("白－沙上古", "鄭張上古", "中唐", "中世朝鮮","國語", "普通話","日語唐音", "日語慣用音", "日語未歸類字音"):
			自.爲音 = False
		if not 音組: 音組.append(音)
		l = list()
		for 音 in 音組:
			if 自.爲音:
				音 = 音.replace(";", "/")
			if 自.info.get("字表使用調值", False):
				音 = 自.轉調類(音)
			音 = 自.正音(音)
			if not 音: continue
			l.append((字, 音, 註))
		return l