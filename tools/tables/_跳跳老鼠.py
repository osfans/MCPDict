#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	註序 = True
	聲 = ""
	韻 = ""
	聲韻 = ""

	def 析(自, 列):
		名 = 自.簡稱
		音 = ""
		調 = ""
		if 名 in ("臨川","奉新宋埠"):
			聲韻, 調, 組 = 列[:3]
			if 聲韻:
				自.聲韻 = 聲韻
			else:
				聲韻 = 自.聲韻
		elif 名 in ("先秦"):
			自.爲音 = False
			音, _, 組 = 列[:3]
		elif 名 in ("景寧東坑",):
			if len(列) == 1 or not 列[1].strip():
				自.韻 = 列[0].strip()
				return
			音, 組 = 列[:2]
			音 = 音.strip().replace(" ", "")
			音 = re.sub("^[無øØ]", "0", 音)
			digits = "12345678"
			if 音 in digits:
				音 = 自.聲韻 + 音
			elif 自.韻:
				自.聲 = 音.rstrip(digits)
				自.聲韻 = 自.聲 + 自.韻
				音 = 自.聲韻 + 音[-1]
			else:
				自.聲韻 = 音.rstrip(digits)
			組 = 組.replace("，", "(文)").replace("。", "(白)").replace("!", "(小稱)").replace(".", "(又)").replace("?", "(存疑)").replace(")(", " ")
			組 = 自.normS(組)
		elif 名 in ("平陰東阿",):
			聲韻, 調, _, 組 = 列[:4]
			if 聲韻:
				自.聲韻 = 聲韻
			else:
				聲韻 = 自.聲韻
			音 = 聲韻 + 調
			組 = 自.normS(組)
		elif 名 in ("望城"):
			聲韻, 調, 組 = 列[:3]
			組 = 組.replace("?", "□")
		elif 名 in ("天台東鄕"):
			if not 列[0]: return
			果 = re.findall(r"^(.*?)(\d+)(.+)$", 列[0])
			if not 果:
				韻 = 列[0].strip()
				if 韻:
					自.韻 = 韻
				return
			聲, 調, 組 = 果[0]
			if 聲:
				自.聲 = 聲
			音 = 自.聲 + 自.韻 + 調
			組 = 自.normS(組)
		elif 名 in ("成都","靑島"):
			列 = "".join(列).strip()
			if not 列: return
			果 = re.findall(r"^(.*?)\[?(\d+)\]?(.+)$", 列)
			if not 果:
				韻 = 列
				if 韻:
					自.韻 = 韻
				return
			聲, 調值, 組 = 果[0]
			if 聲:
				自.聲 = 聲
			音 = 自.轉調類(自.聲 + 自.韻 + 調值)
			組 = 自.normS(組.strip())
		elif 名 in ("無錫"):
			if len(列) < 2: return
			音, 組 = 列[:2]
			組 = 組.replace(" ", "")
			組 = 自.normS(組)
		elif 名 in ("泉州"):
			if len(列) < 2: return
			音, 組 = 列[:2]
			組 = re.sub(r"\[(.)\]", "\\1@", 組)
			組 = 自.normS(組)
		elif 名 in ("思南塘頭",):
			聲韻, 調, 組 = 列[:3]
			組 = 組.replace(" ", "")
			組 = 自.normS(組)
		elif 名 in ("桃源薛家沖",):
			聲韻, 調, _, _, 組 = 列[:5]
			組 = 組.replace(")(", "；")
			組 = 自.normS(組)
		elif 名 in ("通東呂四"):
			聲韻, 調, _, 組 = 列[:4]
			組 = 組.replace(")(", "；")
			組 = re.sub(r"(\d)([-=])", "\\2\\1", 組)
			組 = 自.normS(組)
		elif 名 in ("通城大坪",):
			_, 聲韻, 調, 組 = 列[:4]
			調 = 調.strip("[]")
		elif 名 in ("灌陽","全州文橋", "宜章巖泉",):
			聲韻, 調, 組 = 列[:3]
			組 = 自.normG(組)
		elif 名 in ("江華河路口", "全州黃沙河", "安仁新洲", "1935長沙", "長沙黃花"):
			聲韻, 調, 組 = 列[:3]
			組 = 自.normS(組)
		elif 名 in ("江華粟米塘"):
			聲韻, 調, 組 = 列[:3]
			組 = 自.normS(組)
			組 = 自.normM(組)
		elif 名 in ("東海",):
			果 = re.findall(r"^(.+?)\[(\d+)\][ ]*?(.+)$", "".join(列))
			if not 果: return
			聲韻, 調值, 組 = 果[0]
			調 = 自.僅轉調類(調值)
			組 = 自.normG(組)
			組 = 組.replace("*", "")
		elif 名 in ("孝昌小河",):
			果 = re.findall(r"^(.+?)(\d+) ?(.+)$", 列[0])
			if not 果: return
			聲韻, 調值, 組 = 果[0]
			調 = 自.僅轉調類(調值)
		elif 名 in ("洞口",):
			音, 組 = 列[:2]
		elif 名 in ("欽州正","道縣壽雁", "江永桃川", "桂陽敖泉"):
			聲韻, 調, 組 = 列[:3]
			組 = 自.normG(組)
		elif 名 in ("唐山-開平"):
			聲韻, 調值, 組 = 列[:3]
			調 = 自.僅轉調類(調值)
		elif 名 in ("太原"):
			聲, 韻, 調值, 組 = 列[:4]
			組 = 自.normG(組)
			調 = 自.僅轉調類(調值[2:])
			音 = 聲 + 韻 + 調
		elif 名 in ("汨羅沙溪",):
			聲韻, 調值, 組 = 列[:3]
			組 = 自.normS(組)
			調值 = 調值.replace("42", "24")
			調 = 自.僅轉調類(調值)
		elif 名 in ("長沙雙江",):
			聲韻, 調, _, 組 = 列[:4]
			組 = 自.normS(組)
		elif 名 in ("會同髙椅","會同靑朗", "臨武", "永修梅棠"):
			聲韻, _, 調, 組 = 列[:4]
		elif 名 in ("1884甯城",):
			_,_,組,聲,韻 = 列[:5]
			音 = 聲 + 韻
			if not 音: return
		elif 名 in ("邵東斫曹","綏寧武陽","天柱江東"):
			聲韻, 調 = 列[:2]
			組 = "".join(列[2:]).replace("\t", "").strip()
		elif 名 in ("1930淮安",):
			_, 聲韻, 調, 組 = 列[:4]
			組 = 自.normS(組)
		elif 名 in ("吉安雲樓",):
			聲韻, 調值, 組 = 列[:3]
			調 = 自.僅轉調類(調值)
			組 = 自.normS(組)
		elif 名 in ("平江",):
			聲韻, 調值, 組 = 列[:3]
			音 = 自.轉調類(聲韻 + 調值)
			組 = 自.normS(組)
		elif 名 in ("濟南", "西安", "杭州"):
			聲韻, 調值, 組 = 列[1:4]
			調值 = re.sub(r"^[^\d]+", "", 調值)
			調 = 自.僅轉調類(調值)
			組 = 組.replace(", ", "")
			組 = 自.normG(組)
		elif 名 in ("梅縣", ):
			果 = re.findall(r"^([^\d]*\d+)(.*?)$", "".join(列))
			if 果:
				音, 組 = 果[0]
				組 = 組.replace("}{", " ").strip()
				組 = 自.normG(組)
				音 = 自.轉調類(音)
			else:
				return
		elif 名 in ("江永上江墟",):
			女書, _, 組, 拼音 = 列[:4]
			拼音 = re.sub("^h", "x", 拼音)
			拼音 = 拼音.replace("nj", "ȵ").replace('ng', 'ŋ').replace("c", "ɕ").replace('h', 'ʰ')
			拼音 = 拼音.replace("oe", "ø").replace('e', 'ə').replace('iə', 'ie').replace('w', 'ɯ')
			音 = 自.轉調類(拼音) + 女書
		elif 名 in ("揚州",):
			自.simplified = 0
			音, 組 = 列[:2]
			組 = 自.normS(組, "（\\1）")
			l = ""
			if "（" in 音:
				說明 = re.search(r"（.*?）", 音).group()
				音 = 音.replace(說明, "")
				說明 = 說明.replace("?", "存疑")
			else:
				說明 = ""
			marks = "?!%+，。"
			組 = re.sub(r"(（.*?）)([\?\!%+，。])?", "\\2\\1", 組)
			for 字,c,註 in re.findall(r"(.)([\?\!%+，。])?(（[^）]*?（.*?）.*?）|（.*?）)?", 組):
				if 註:
					註 = 註[1:-1]
					if 註[0] in marks and not c:
						c = 註[0]
						註 = 註[1:]
				p = ""
				if c == '+':
					p = "書"
					c = ""
				elif c == '!':
					c = "*"
				elif c == '%':
					p = "又音"
					c = ""
				elif c == '，':
					p = "外"
					c = ""
				elif c == '。':
					p = "非本字正字"
					c = ""
				if p:
					註 = f"({p}){註}"
				l += f"{字}{c}[{說明}{註}]"
			組 = l
		elif 名 in ("湘潭易俗河", "瀘溪梁家潭", "湘鄕棋梓"):
			聲韻, 調, _, 組 = 列[:4]
		elif len(列) > 3 and 列[3]:
			聲韻, 調, _, 組 = 列[:4]
			組 = 組.replace(")(", ";")
			組 = re.sub(r"(\d)([-=])", "\\2\\1", 組)
			組 = 自.normS(組)
		else:
			聲韻, 調, 組 = 列[:3]
		if not 音: 音 = 聲韻 + 調
		音 = 自.正音(音, True)
		l = list()
		if "｛" not in 組:
			組 = 自.normM(組)
		if "｛" not in 組:
			組 = 自.normG(組)
		if "｛" not in 組:
			組 = 自.normS(組)
		組 = re.sub(r"(｛.*?｝)([-=])", "\\2\\1", 組)
		for 字, c, 號, 註 in re.findall(r"(.)([-=*?@+]?)(\d?) *(｛.*?｝)?", 組):
			if 註: 註 = 註[1:-1]
			註 = 號 + 註
			l.append((字, 音 + c, 註))
			if 音 not in 自.音表: 自.音表[音] = list()
			自.音表[音].append(字)
		return l