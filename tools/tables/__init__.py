#!/usr/bin/env python3

import datetime
import os, re, json, glob
from importlib import import_module
import tables._詳情
from pypinyin import pinyin, Style
from collections import defaultdict
from itertools import combinations
from opencc import OpenCC

SOURCE = "data"
TARGET = "output"
PATH = os.path.dirname(os.path.abspath(__file__))

VARIANT_FILE = os.path.join(PATH, SOURCE, "正字.tsv")

辭典 = ["漢字","說文","康熙","匯纂","漢大"]
辭典數 = len(辭典)
形碼 = ["異體字","字形變體","字形描述","部件檢索","兩分","總筆畫數","部首餘筆","五筆畫","五筆86","五筆98","五筆06","倉頡三代","倉頡五代","倉頡六代","山人","分類"]

省集 = {'山西', '貴州', '甘肅', '內蒙古', '澳門', '四川', '山東', '臺灣', '雲南', '廣東', '江蘇', '海外', '吉林', '廣西', '香港', '黑龍江', '河南', '河北', '湖南', '上海', '海南', '寧夏', '北京', '遼寧', '新疆', '安徽', '福建', '重慶', '湖北', '浙江', '靑海', '江西', '陝西', '天津', '西藏'}

n2o_dict = {}
o2n_dict = {}

for 行 in open("tables/data/mulcodechar.dt", encoding="U8"):
	if not 行 or 行[0] == "#": continue
	列 = 行.strip().split("-")
	if len(列) < 2: continue
	n2o_dict[列[0]] = 列[1]
	o2n_dict[列[1]] = 列[0]

opencc_t2s = OpenCC("t2s.json")

def n2o(s):
	if not s: return ""
	t = ""
	for i in s:
		t += n2o_dict.get(i, i)
	return t

def o2n(s):
	if not s: return ""
	t = ""
	for i in s:
		t += o2n_dict.get(i, i)
	return t

def t2s(s, level=2):
	s = o2n(s)
	if level == 1:
		return s
	return opencc_t2s.convert(s)

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
	return 0x3400<=n<0xA000 or n in (0x25A1, 0x3007) or 0xF900<=n<0xFB00 or 0x20000<=n<=0x323AF and not 爲兼容字(c)

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
	return d

normVariants = getSTVariants(1)
stVariants = getSTVariants(2)

def s2t(字組, level=1):
	t = ""
	for 字 in 字組:
		if level == 1:
			字 = normVariants.get(字, 字)
		else:
			字 = stVariants.get(字, 字)
		t += 字
	return t

def addAllFq(d, fq, order,不加片 = False):
	if order is None or fq is None: return
	fqs = fq.split(",")[0].split("-")
	for i in range(len(fqs)):
		名 = "-".join(fqs[0:i+1])
		if not 名: continue
		if 不加片 and 名.endswith("片"): continue
		order = "-".join(order.split("-")[0:i+1])
		if 名 in d:
			if d[名] < order: continue
		d[名] = order

def addCfFq(d, fq, order):
	if fq is None: return
	列 = fq.split(",")
	fqs = 列[0].split("-")
	for i in range(len(fqs)):
		name = "-".join(fqs[0:i+1])
		if not name: continue
		order = "-".join(order.split("-")[0:i+1])
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
			path = os.path.dirname(a)
			for i in infos:
				if a in glob.glob(os.path.join(path, infos[i]["文件名"])):
					l.append(i)
					break
	return l

def 獲取同音字頻(get=False):
	if not get: return
	同音字頻 = defaultdict(int)
	詳情 = tables._詳情.加載()
	for mod,d in 詳情.items():
		try:
			if d["文件格式"]:
				語 = import_module(f'tables._{d["文件格式"]}').表()
				語.setmod(mod)
			else:
				語 = import_module(f"tables.{mod}").表()
			if not 語.文件名: 語.文件名 = d["文件名"]
		except:
			continue
		if "繁" not in d["繁簡"]: 語.simplified = 2
		if d["地圖集二分區"] == None: d["地圖集二分區"] = ""
		if "聯表列名" in d:
			a = d["聯表列名"].upper()
			語.音列 = sum([26**(len(a)-1-i)*(ord(j)-ord('A')+1) for i,j in enumerate(a)]) - 1
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
				if len(字組) < 2: continue
				for 項 in combinations(字組, 2):
					雙字 = "".join(sorted(項))
					同音字頻[雙字] += 1
	return 同音字頻

def getLangs(dicts, 參數, args):
	省 = args.省
	同音字頻 = 獲取同音字頻(args.c)
	詳情 = tables._詳情.加載(省)
	語組 = []
	數 = 0
	if len(參數) == 1:
		mods = ["漢字"]
		mods.extend(getLangsByArgv(詳情, 參數))
	else:
		mods = 辭典.copy()
		mods.extend(getLangsByArgv(詳情, 參數) if 參數 else 詳情.keys())
		mods.extend(形碼)
	types = [dict(),dict(),dict()]
	省 = defaultdict(int)
	推薦人 = defaultdict(int)
	維護人 = defaultdict(int)
	keys = None
	t = open("warnings.txt", "w", encoding="U16")
	for mod in mods:
		if mod in 詳情:
			d = 詳情[mod]
			try:
				if d["文件格式"]:
					語 = import_module(f'tables._{d["文件格式"]}').表()
					語.setmod(mod)
				else:
					語 = import_module(f"tables.{mod}").表()
				if not 語.文件名: 語.文件名 = d["文件名"]
			except Exception as e:
				print(f"\t\t\t{e} {mod}")
				continue
			if d.pop("是否有人在做") != "已做":
				print(f"{語} 不是已做")
			if "繁" not in d["繁簡"]: 語.simplified = 2
			if d["地圖集二分區"] == None: d["地圖集二分區"] = ""
			if "聯表列名" in d:
				a = d["聯表列名"].upper()
				語.音列 = sum([26**(len(a)-1-i)*(ord(j)-ord('A')+1) for i,j in enumerate(a)]) - 1
			addAllFq(types[0], d["地圖集二分區"], d["地圖集二排序"])
			addAllFq(types[1], d["音典分區"], d["音典排序"])
			addCfFq(types[2], d["陳邡分區"], d["陳邡排序"])
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
			語.加載(dicts, 更新=args.c)
			if d["文件名"] != "mcpdict.db":
				if 語.字數 == 0: continue
				if 語.字數 < 900:
					print(f"{語} 字數太少: {語.字數}")
				elif 語.聲韻數 < 100:
					print(f"{語} 音節太少: {語.聲韻數}")
			if not d["無調"] and not 調典:
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
			if 同音字頻:
				if 語.檢查同音字() and 語.字數 < 10000:
					for 音, 字組 in 語.聲韻典.items():
						if len(字組) < 2: continue
						for 字甲 in 字組:
							字頻 = 0
							字組乙 = set(字組)
							字組乙.remove(字甲)
							n = len(字組乙)
							for 字乙 in 字組乙:
								字頻 += 同音字頻["".join(sorted((字甲, 字乙)))]
							if 字頻 < 1.8 * n:
								語.誤.append(f"【{字甲}】可能不讀[{音}]{''.join(字組乙)[:4]}")
			語.info["解析日志"] = None
			語.info["同音字表"] = None
			if 語.誤:
				語.info["解析日志"] = "\n".join(語.誤)
				all_editors = ",".join(editor)
				語.全稱 = 語.info["語言"]
				print(f"{語.全稱}（{語}）-{語.文件名}-{all_editors}", file=t)
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
			語.加載(dicts)
		語.info["字數"] = 語.字數
		語.info["□數"] = 語.框數 if 語.框數 else None
		音節數 = 語.音節數
		聲韻數 = 語.聲韻數
		語.info["音節數"] = 音節數 if 音節數 else None
		語.info["不帶調音節數"] = 聲韻數 if 聲韻數 and 聲韻數 != 音節數 else None
		語.info["網站"] = 語.網站
		語.info["網址"] = 語.網址
		lang_t = 語.info["語言"]
		lang_s = t2s(語.info["語言"], 2)
		if lang_s not in lang_t:
			lang_t += f",{lang_s}"
		lang_s = t2s(語.info["語言"], 1)
		if lang_s not in lang_t:
			lang_t += f",{lang_s}"
		語.info["語言索引"] = lang_t
		if 語.說明: 語.info["說明"] = 語.說明
		if not keys: keys = 語.info.keys()
		語組.append(語)
	t.close()
	字 = 語組[0]
	for 項 in keys:
		if 項 not in 字.info: 字.info[項] = None
	字.info["字數"] = len(dicts)
	字.info["說明"] = "語言數：%d\n\n%s"%(數, 字.說明)
	省表 = sorted(省集, key=普拼)
	if "海外" in 省表:
		省表.remove("海外")
		省表.append("海外")
	字.info["省"] = ",".join([f"{i} ({省[i]})" for i in 省表])
	字.info["維護人"] = ",".join([f"{i} ({維護人[i]})" for i in sorted(維護人.keys(), key=普拼)])
	字.info["推薦人"] = ",".join([f"{i} ({推薦人[i]})" for i in sorted(推薦人.keys(), key=普拼)])
	字.info["地圖集二分區"] = ",".join(sorted(types[0].keys(),key=lambda x:types[0][x]))
	字.info["音典分區"] = ",".join(sorted(types[1].keys(),key=lambda x:types[1][x]))
	字.info["陳邡分區"] = ",".join(sorted(types[2].keys(),key=lambda x:types[2][x]))
	字.info["版本"] = datetime.datetime.now().strftime("%Y-%m-%d")
	print("語言數", 數)
	return 語組
