#!/usr/bin/env python3

import datetime
import os, re, json, fnmatch
from importlib import import_module
import tables._詳情
from pypinyin import pinyin, Style
from collections import defaultdict
from itertools import combinations
from opencc import OpenCC
import sqlite3
import logging

logging.basicConfig(format='[%(asctime)s,%(msecs)03d] %(message)s', level=logging.INFO, datefmt='%H:%M:%S',)
SOURCE = "data"
TARGET = "output"
PATH = os.path.dirname(os.path.abspath(__file__))
if PATH.endswith("__init__"): PATH = PATH[:-8]
WORKSPACE = os.path.join(PATH, "..")

VARIANT_FILE = os.path.join(PATH, SOURCE, "正字.tsv")

辭典 = ["漢字","說文","康熙","匯纂","漢大", "異體字","字形變體","字形描述","部件檢索","兩分","總筆畫數","部首餘筆","五筆畫","五筆86","五筆98","五筆06","倉頡三代","倉頡五代","倉頡六代","山人","分類"]

_t2s = OpenCC("t2s.json")
_s2t = OpenCC("s2t.json")

def opencc_s2t(s):
	return _s2t.convert(s)

def t2s(s, level=2):
	if level == 1:
		return s
	return _t2s.convert(s)

def hex2chr(uni):
	"把unicode轉換成漢字"
	if uni.startswith("U+"): uni = uni[2:]
	return chr(int(uni, 16))

hzorders = dict()
def cjkorder(s):
	return hzorders.get(s, [0x100, ord(s)])

def 爲兼容字(c):
	n = ord(c)
	return (0xF900 <= n < 0xFB00 and c not in '﨎﨏﨑﨓﨔﨟﨡﨣﨤﨧﨨﨩' or 0x2F800 <= n < 0x2FA20)

def 爲字(c):
	c = c.strip()
	if len(c) != 1: return False
	n = ord(c)
	return 0x3400<=n<0xA000 or n in (0x25A1, 0x3007) or 0xF900<=n<0xFB00 or 0x20000<=n<=0x3347F and not 爲兼容字(c)

def 找字(字組):
	index = len(字組)
	for i, c in enumerate(字組):
		if 爲字(c):
			index = i
			break
	return index

def 有字(字組):
	return any(map(爲字, 字組))

def 普拼(word):
	return pinyin(t2s(word), style=Style.TONE3, heteronym=False) if 爲字(word[0]) else [[word.lower()]]

def getSTVariants(level=2):
	d = dict()
	for 行 in open(VARIANT_FILE,encoding="U8"):
		if 行.startswith("#"): continue
		列 = 行.strip().split("\t")
		if level == 1 and "#" in 行:
			continue
		列[1] = 列[1].split("#")[0].strip()
		if " " not in 列[1]:
			d[列[0]] = 列[1]
	return str.maketrans(d)

normVariants = getSTVariants(1)
stVariants = getSTVariants(2)

def s2t(字組, level=1):
	if level == 1:
		return 字組.translate(normVariants)
	else:
		return 字組.translate(stVariants)

方言調查字表 = set()
for 行 in open(os.path.join(PATH, SOURCE, "方言調查字表.tsv"), encoding="U8"):
	if not 行 or 行[0] == "#": continue
	列 = 行.strip().split("\t")
	字 = 列[1]
	if len(字) != 1: continue
	方言調查字表.add(字)

def addAllFq(d, fq, order,不加片 = False):
	if order is None or fq is None: return
	FS = "－"
	fqs = fq.split(",")[0].split(FS)
	for i in range(len(fqs)):
		名 = FS.join(fqs[0:i+1])
		if not 名: continue
		if 不加片 and 名.endswith("片"): continue
		order = FS.join(order.split(FS)[0:i+1])
		if 名 in d:
			if d[名] < order: continue
		d[名] = order

def addCfFq(d, fq, order):
	if fq is None: return
	列 = fq.split(",")
	FS = "－"
	fqs = 列[0].split(FS)
	for i in range(len(fqs)):
		name = FS.join(fqs[0:i+1])
		if not name: continue
		order = FS.join(order.split(FS)[0:i+1])
		if name in d:
			if d[name] < order: continue
		d[name] = order
		if len(列) >= 2:
			d[列[1]] = ""

def getLangsByArgv(infos, argv):
	l = []
	for a in argv:
		if a in infos:
			l.append(a)
		elif os.path.isfile(a):
			ba = os.path.basename(a)
			ba2 = re.sub(r"( ?\(\d\d?\))(.[^.]*)$", "\\2", ba)
			ba3 = ba2.replace(".tsv", ".docx")
			ba4 = ba2.replace(".tsv", ".xlsx")
			bas = [ba, ba2, ba3, ba4]
			for i in infos:
				if fnmatch.filter(bas, infos[i]["文件名"]):
					l.append(i)
					break
	return l

def 列序(a):
	return sum([26**(len(a)-1-i)*(ord(j)-ord('A')+1) for i,j in enumerate(a)]) - 1

def getDicts(dicts):
	字書 = list()
	for mod in 辭典:
		語 = import_module(f"tables.{mod}").表()
		d = dict()
		d["語言"] = 語.全稱 if 語.全稱 else mod
		d["簡稱"] = 語.簡稱 if 語.簡稱 else mod
		d["地圖集二顏色"] = 語.顏色 if 語.顏色 else None
		d["地圖集二分區"] = None
		語.info = d
		語.加載(dicts)
		if 語.字書:
			字書.append(語)
	fls = ["SW","KX","HZ","HD"]
	for _, d in dicts.items():
		for i, k in enumerate(辭典[1:5]):
			if k in d:
				d["分類"] = (d["分類"] + "\t" + fls[i]) if "分類" in d else fls[i]
	return 辭典, 字書

def 獲取同音字頻(get=False):
	同音字頻 = defaultdict(set)
	高頻字 = list()
	if not get: return 同音字頻, 高頻字
	同音字頻表 = "同音字頻"
	if os.path.exists(f"{同音字頻表}.db"):
		conn = sqlite3.connect(f"{同音字頻表}.db")
		c = conn.cursor()
		c.execute(f"select 漢字, 頻率 from {同音字頻表}")
		n = 0
		for result in c.fetchall():
			字 = result[0]
			頻率 = result[1]
			if n == 0:
				高頻字 = 頻率
			else:
				同音字頻[字] = set(頻率.split(","))
			n += 1
		conn.commit()
		conn.close()
		return 同音字頻, 高頻字
	高頻字表 = defaultdict(int)
	詳情 = tables._詳情.加載()
	for mod,d in 詳情.items():
		try:
			if d["字表格式"]:
				語 = import_module(f'tables._{d["字表格式"]}').表()
				語.setmod(mod)
			else:
				語 = import_module(f"tables.{mod}").表()
			if not 語.文件名: 語.文件名 = d["文件名"]
		except:
			continue
		if "正" in d["繁簡"]:
			語.simplified = 0
		elif "繁" in d["繁簡"]:
			語.simplified = 1
		else:
			語.simplified = 2
		if d["地圖集二分區"] == None: d["地圖集二分區"] = ""
		if d["字聲韻調註列名"]:
			字聲韻調註列名 = d["字聲韻調註列名"]
			列名 = 字聲韻調註列名.split(",") if "," in 字聲韻調註列名 else list(字聲韻調註列名)
			語.列序 = [列序(i) for i in 列名]
		if d["聲調"]:
			調典 = dict()
			調組 = json.loads(d["聲調"])
			for 調 in 調組:
				調值 = 調組[調][0]
				if 調值 in 調典 and "入" in 調組[調][3]:
					調值 += "0"
				調典[調值] = 調
			語.調典 = 調典
		語.info = d
		語.讀()
		if 語.音節數 > 0:
			for 字組 in 語.聲韻典.values():
				字組 = list(filter(爲字, 字組))
				for 字 in 字組:
					高頻字表[字] += 1
				if len(字組) < 2: continue
				for 項 in combinations(字組, 2):
					雙字 = "".join(sorted(項))
					同音字頻[雙字].add(語.簡稱)
	高頻字 = "□" + ("".join(sorted(高頻字表.keys(), key=高頻字表.get, reverse=True)[:5000]))
	for i in set(同音字頻.keys()):
		if len(同音字頻[i]) <= 1:
			del 同音字頻[i]
	fields = ["漢字", "頻率"]
	CREATE = 'CREATE VIRTUAL TABLE %s USING fts3 (%s)' % (同音字頻表, ",".join(fields))
	INSERT = 'INSERT INTO %s VALUES (%s)'% (同音字頻表, ','.join('?' * len(fields)))
	conn = sqlite3.connect(f"{同音字頻表}.db")
	c = conn.cursor()
	c.execute(CREATE)
	c.execute(INSERT, ("", 高頻字))
	c.executemany(INSERT, ((i, ",".join(j)) for i,j in 同音字頻.items()))
	conn.commit()
	conn.close()
	return 同音字頻, 高頻字

def getLangs(items, 參數, args):
	省 = args.省
	同音字頻, 高頻字 = 獲取同音字頻(len(參數) != 1 or args.s or args.c)
	計算相似度 = args.s
	詳情 = tables._詳情.加載(省)
	語組 = []
	數 = 0
	mods = []
	if not args.output: mods.append("漢字")
	mods.extend(getLangsByArgv(詳情, 參數) if 參數 else 詳情.keys())
	語言組 = set(詳情.keys())
	types = [dict(),dict(),dict()]
	省 = defaultdict(int)
	推薦人 = defaultdict(int)
	維護人 = defaultdict(int)
	keys = None
	if 計算相似度:
		高頻雙字 = []
		for 字甲, 字乙 in combinations(高頻字, 2):
			雙字 = sorted((字甲, 字乙))
			if "".join(雙字) in 同音字頻:
				高頻雙字.append(雙字)
	t = open("warnings.txt", "w", encoding="U16")
	for mod in mods:
		if mod in 詳情:
			d = 詳情[mod]
			try:
				if d["字表格式"]:
					語 = import_module(f'tables._{d["字表格式"]}').表()
					語.setmod(mod)
				else:
					語 = import_module(f"tables.{mod}").表()
				if not 語.文件名: 語.文件名 = d["文件名"]
			except Exception as e:
				print(f"\t\t\t{e} {mod}")
				continue
			if "正" in d["繁簡"]:
				語.simplified = 0
			elif "繁" in d["繁簡"]:
				語.simplified = 1
			else:
				語.simplified = 2
			if d["地圖集二分區"] == None: d["地圖集二分區"] = ""
			if d["字聲韻調註列名"]:
				字聲韻調註列名 = d["字聲韻調註列名"]
				列名 = 字聲韻調註列名.split(",") if "," in 字聲韻調註列名 else list(字聲韻調註列名)
				語.列序 = [列序(i) for i in 列名]
			addAllFq(types[0], d["地圖集二分區"], d["地圖集二排序"])
			addAllFq(types[1], d["音典分區"], d["音典排序"])
			addCfFq(types[2], d["陳邡分區"], d["陳邡排序"])
			if d["聲調"]:
				調典 = dict()
				調組 = json.loads(d["聲調"])
				不計入調 = set()
				for 調 in 調組:
					調值 = 調組[調][0]
					調名 = 調組[調][3]
					if 調值 in 調典 and "入" in 調名:
						調值 += "0"
					調典[調值] = 調
					if "變調" in 調名 or "輕聲" in 調名 or "小" in 調名 or 調.startswith("0"):
						不計入調.add(調)
				語.調典 = 調典
				語.不計入調 = 不計入調
			語.info = d
			# print(d["文件名"])
			語.加載條目(items, 更新=args.c)
			if d["文件名"] != "mcpdict.db":
				if 語.字數 == 0:
					if 語.spath: print(f"{語} 未成功解析")
					continue
				if 語.字數 < 500:
					print(f"{語} 字數太少: {語.字數}")
					if mod in 語言組: 語言組.remove(mod)
				elif 語.聲韻數 < 100:
					print(f"{語} 音節太少: {語.聲韻數}")
				elif "一" in 語.d and len(語.d["一"]) > 4:
					print(f"{語} 格式可能有誤：{語.d['一']}")
			if not 語.無調():
				if 調典:
					差異 = 語.聲調典[語.簡稱] - 調組.keys()
					if '0' in 差異: 差異.remove('0')
					if 差異:
						誤 = f"未登記聲調：{','.join(差異)}"
						print(f"{語} {誤}")
						語.誤.append(誤)
				else:
					print(f"{語} 無調值")
			語.info["文件名"] = 語.文件名
			if d["省"]:
				省[d["省"]] += 1
			if d["推薦人"]:
				for 人 in d["推薦人"].split(","):
					人 = 人.strip()
					if 人:
						推薦人[人] += 1
			editors = [set(d[i].split(",")) for i in ("作者", "錄入人", "維護人") if d[i]]
			editor = set()
			for 人 in editors:
				editor.update(人)
			for 人 in editor:
				人 = re.sub("（.*?）", "", 人).strip()
				if 人:
					維護人[人] += 1
			數 += 1
			if 同音字頻 and args.c and 語.檢查同音字() and 語.字數 < 10000:
				for 音, 字組 in 語.聲韻典.items():
					字組 = list(filter(爲字, 字組))
					if len(字組) < 2: continue
					for 字甲 in 字組:
						字頻 = 0
						字組乙 = set(字組)
						字組乙.remove(字甲)
						n = len(字組乙)
						for 字乙 in 字組乙:
							字頻 += len(同音字頻["".join(sorted((字甲, 字乙)))])
						if 字頻 == 0:
							語.誤.append(f"【{字甲}】可能不讀[{音}]{''.join(字組乙)[:4]}")
			if 計算相似度 and mod in 語言組:
				相似度 = defaultdict(int)
				雙字數 = 0
				for 字甲, 字乙 in 高頻雙字:
					if 字甲 not in 語.d or 字乙 not in 語.d: continue
					同音 = 同音字頻.get(字甲 + 字乙)
					if mod in 同音:
						同音.remove(mod)
						for 語乙 in 同音:
							相似度[語乙] += 1
					else:
						補集 = 語言組 - 同音
						補集.remove(mod)
						# if len(同音) < 10: continue
						for 語乙 in 補集:
							相似度[語乙] += 1
					雙字數 += 1
				語.info["相似度"] = ",".join(map(lambda x:f"{x}({相似度[x] * 100 / 雙字數:.2f}%)", sorted((i for i in 相似度), key=相似度.get, reverse=True)[:10]))
				logging.info(f"{語.簡稱}:{語.info['相似度']}")
			語.info["解析日志"] = None
			語.info["同音字表"] = None
			if 語.誤:
				語.info["解析日志"] = "\n".join(語.誤)
				all_editors = ",".join(editor)
				語.全稱 = 語.info["語言"]
				print(f"{語.全稱}（{語}）-{語.文件名}-{all_editors} *{len(語.誤)}", file=t)
				for 誤 in 語.誤:
					print(f"\t{誤}", file=t)
			if 語.音表:
				同音字表 = ""
				上聲韻 = ""
				for 音, 字組 in 語.音表.items():
					聲韻, 調 = 語.分音(音)
					if 上聲韻 == 聲韻:
						同音字表 += "\t"
					else: 
						同音字表 += "\n" + 聲韻
						上聲韻 = 聲韻
					同音字表 += f"[{調}]{''.join(字組[:4])}"
				語.info["同音字表"] = 同音字表.strip()
		else:
			語 = import_module(f"tables.{mod}").表()
			d = dict()
			d["語言"] = 語.全稱 if 語.全稱 else mod
			d["簡稱"] = 語.簡稱 if 語.簡稱 else mod
			d["地圖集二顏色"] = 語.顏色 if 數 == 0 else None
			d["地圖集二分區"] = None
			語.info = d
			語.加載條目(items)
		語.info["字數"] = 語.字數
		語.info["□數"] = 語.框數 if 語.框數 else None
		音節數 = 語.音節數
		聲韻數 = 語.聲韻數
		語.info["音節數"] = 音節數 if 音節數 else None
		語.info["不帶調音節數"] = 聲韻數 if 聲韻數 and 聲韻數 != 音節數 else None
		if 語.說明: 語.info["說明"] = 語.說明
		if not keys: keys = 語.info.keys()
		if args.output:
			語.存(args.output)
		語組.append(語)
	t.close()
	字 = 語組[0]
	for 項 in keys:
		if 項 not in 字.info: 字.info[項] = None
	if 計算相似度:
		字.info["相似度"] = None
	字.info["說明"] = "語言數：%d\n\n%s"%(數, 字.說明)
	省表 = sorted(省.keys(), key=普拼)
	if "海外" in 省表:
		省表.remove("海外")
		省表.append("海外")
	字.info["省"] = ",".join([f"{i} ({省[i]})" for i in 省表])
	字.info["維護人"] = ",".join([f"{i} ({維護人[i]})" for i in sorted(維護人.keys(), key=普拼)])
	字.info["推薦人"] = ",".join([f"{i} ({推薦人[i]})" for i in sorted(推薦人.keys(), key=普拼)])
	字.info["地圖集二分區"] = ",".join(sorted(types[0].keys(),key=types[0].get))
	字.info["音典分區"] = ",".join(sorted(types[1].keys(),key=types[1].get))
	字.info["陳邡分區"] = ",".join(sorted(types[2].keys(),key=types[2].get))
	字.info["版本"] = datetime.datetime.now().strftime("%Y-%m-%d")
	if 語組[-1].韻母集:
		print("韻母表", ",".join(sorted(語組[-1].韻母集)).strip(","))
	if 語組[-1].聲母集:
		print("聲母表", ",".join(sorted(語組[-1].聲母集)).strip(","))
	if not args.output: print("語言數", 數)
	return 語組, 高頻字
