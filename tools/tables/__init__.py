#!/usr/bin/env python3

import datetime
import os, re, json, glob
from importlib import import_module
import tables._詳情
from pypinyin import pinyin, Style
from collections import defaultdict
from opencc import OpenCC

SOURCE = "data"
TARGET = "output"
VARIANT_FILE = f"tables/{SOURCE}/正字.tsv"

辭典 = ["漢字","說文","康熙","匯纂","漢大"]
辭典數 = len(辭典)
形碼 = ["異體字","字形變體","字形描述","部件檢索","兩分","總筆畫數","部首餘筆","五筆畫","五筆86版","五筆98版","五筆06版","倉頡三代","倉頡五代","倉頡六代","山人碼LTS","分類"]

省_set = {'山西', '貴州', '甘肅', '內蒙古', '澳門', '四川', '山東', '臺灣', '雲南', '廣東', '江蘇', '海外', '吉林', '廣西', '香港', '黑龍江', '河南', '河北', '湖南', '上海', '海南', '寧夏', '北京', '遼寧', '新疆', '安徽', '福建', '重慶', '湖北', '浙江', '靑海', '江西', '陝西', '天津', '西藏'}

n2o_dict = {}

for line in open("tables/data/mulcodechar.dt", encoding="U8"):
	if not line or line[0] == "#": continue
	fs = line.strip().split("-")
	if len(fs) < 2: continue
	n2o_dict[fs[0]] = fs[1]

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
		t += n2o_dict.get(i, i)
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

def isCompatible(c):
	n = ord(c)
	return (0xF900 <= n < 0xFB00 and c not in '﨎﨏﨑﨓﨔﨟﨡﨣﨤﨧﨨﨩' or 0x2F800 <= n < 0x2FA20)

def isHZ(c):
	c = c.strip()
	if len(c) != 1: return False
	n = ord(c)
	return 0x3400<=n<0xA000 or n in (0x25A1, 0x3007) or 0xF900<=n<0xFB00 or 0x20000<=n<=0x323AF and not isCompatible(c)

def get_pinyin(word):
	return pinyin(t2s(word), style=Style.TONE3, heteronym=False) if isHZ(word[0]) else [[word.lower()]]

def getSTVariants(level=2):
	d = dict()
	for line in open(VARIANT_FILE,encoding="U8"):
		if line.startswith("#"): continue
		fs = line.strip().split("\t")
		if level == 1 and "#" in line:
			continue
		fs[1] = fs[1].split("#")[0].strip()
		if " " not in fs[1]:
			d[fs[0]] = fs[1]
	return d

normVariants = getSTVariants(1)
stVariants = getSTVariants(2)

def s2t(hzs, level=1):
	t = ""
	for hz in hzs:
		if level == 1:
			hz = normVariants.get(hz, hz)
		else:
			hz = stVariants.get(hz, hz)
		t += hz
	return t

def addAllFq(d, fq, order,ignorePian = False):
	if order is None or fq is None: return
	fqs = fq.split(",")[0].split("-")
	for i in range(len(fqs)):
		name = "-".join(fqs[0:i+1])
		if not name: continue
		if ignorePian and name.endswith("片"): continue
		order = "-".join(order.split("-")[0:i+1])
		if name in d:
			if d[name] < order: continue
		d[name] = order

def addCfFq(d, fq, order):
	if fq is None: return
	fs = fq.split(",")
	fqs = fs[0].split("-")
	for i in range(len(fqs)):
		name = "-".join(fqs[0:i+1])
		if not name: continue
		order = "-".join(order.split("-")[0:i+1])
		if name in d:
			if d[name] < order: continue
		d[name] = order
		if len(fs) >= 2:
			d[fs[1]] = ""

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

def getLangs(dicts, argv, 省=None):
	infos = tables._詳情.load(省)
	langs = []
	count = 0
	if len(argv) == 1:
		mods = ["漢字"]
		mods.extend(getLangsByArgv(infos, argv))
	else:
		mods = 辭典.copy()
		mods.extend(getLangsByArgv(infos, argv) if argv else infos.keys())
		mods.extend(形碼)
	types = [dict(),dict(),dict()]
	省 = defaultdict(int)
	推薦人 = defaultdict(int)
	維護人 = defaultdict(int)
	keys = None
	t = open("warnings.txt", "w", encoding="U8")
	for mod in mods:
		if mod in infos:
			d = infos[mod]
			try:
				if d["文件格式"]:
					lang = import_module(f'tables._{d["文件格式"]}').表()
					lang.setmod(mod)
				else:
					lang = import_module(f"tables.{mod}").表()
				if not lang._file: lang._file = d["文件名"]
			except Exception as e:
				print(f"\t\t\t{e} {mod}")
				continue
			if d["繁簡"] == "简": lang.simplified = 2
			if d["地圖集二分區"] == None: d["地圖集二分區"] = ""
			addAllFq(types[0], d["地圖集二分區"], d["地圖集二排序"])
			addAllFq(types[1], d["音典分區"], d["音典排序"])
			addCfFq(types[2], d["陳邡分區"], d["陳邡排序"])
			if d["聲調"]:
				toneMaps = dict()
				sds = json.loads(d["聲調"])
				for i in sds:
					tv = sds[i][0]
					if tv in toneMaps and "入" in sds[i][3]:
						tv += "0"
					toneMaps[tv] = i
				lang.toneMaps = toneMaps
			lang.info = d
			lang.load(dicts)
			if d["文件名"] != "mcpdict.db":
				if lang.count == 0: continue
				if lang.count < 900:
					lang.errors.append(f"字數太少: {lang.count}")
				elif lang.syCount < 100:
					lang.errors.append(f"音節太少: {mod}")
			# if not len(toneMaps.keys()):
			# 	lang.errors.append("無調值")
			lang.info["文件名"] = lang._file
			if d["省"]:
				省[d["省"]] += 1
			if d["推薦人"]:
				for i in d["推薦人"].split(","):
					i = i.strip()
					if i:
						推薦人[i] += 1
			editors = [set(d[i].split(",")) for i in ("作者", "錄入人", "維護人") if d[i]]
			editor = set()
			for i in editors:
				editor.update(i)
			for i in editor:
				i = re.sub("（.*?）", "", i).strip()
				if i:
					維護人[i] += 1
			count += 1
			if lang.errors:
				all_editors = ",".join(editor)
				lang.full = lang.info["語言"]
				print(f"{lang.full}（{lang}）-{lang._file}-{all_editors}", file=t)
				for i in lang.errors:
					print(f"\t{i}", file=t)
				lang.errors.clear()
		else:
			lang = import_module(f"tables.{mod}").表()
			d = dict()
			d["語言"] = lang.full if lang.full else mod
			d["簡稱"] = lang.short if lang.short else mod
			d["地圖集二顏色"] = lang.color if count == 0 else None
			d["地圖集二分區"] = None
			lang.info = d
			lang.load(dicts)
		lang.info["字數"] = lang.count
		lang.info["□數"] = lang.unknownCount if lang.unknownCount else None
		sydCount = lang.sydCount
		syCount = lang.syCount
		lang.info["音節數"] = sydCount if sydCount else None
		lang.info["不帶調音節數"] = syCount if syCount and syCount != sydCount else None
		lang.info["網站"] = lang.site
		lang.info["網址"] = lang.url
		lang_t = lang.info["語言"]
		lang_s = t2s(lang.info["語言"], 2)
		if lang_s not in lang_t:
			lang_t += f",{lang_s}"
		lang_s = t2s(lang.info["語言"], 1)
		if lang_s not in lang_t:
			lang_t += f",{lang_s}"
		lang.info["語言索引"] = lang_t
		if lang.note: lang.info["說明"] = lang.note
		if not keys: keys = lang.info.keys()
		langs.append(lang)
	t.close()
	hz = langs[0]
	for i in keys:
		if i not in hz.info: hz.info[i] = None
	hz.info["字數"] = len(dicts)
	hz.info["說明"] = "語言數：%d<br><br>%s"%(count, hz.note)
	省表 = sorted(省_set, key=get_pinyin)
	if "海外" in 省表:
		省表.remove("海外")
		省表.append("海外")
	hz.info["省"] = ",".join([f"{i} ({省[i]})" for i in 省表])
	hz.info["維護人"] = ",".join([f"{i} ({維護人[i]})" for i in sorted(維護人.keys(), key=get_pinyin)])
	hz.info["推薦人"] = ",".join([f"{i} ({推薦人[i]})" for i in sorted(推薦人.keys(), key=get_pinyin)])
	hz.info["地圖集二分區"] = ",".join(sorted(types[0].keys(),key=lambda x:types[0][x]))
	hz.info["音典分區"] = ",".join(sorted(types[1].keys(),key=lambda x:types[1][x]))
	hz.info["陳邡分區"] = ",".join(sorted(types[2].keys(),key=lambda x:types[2][x]))
	hz.info["版本"] = datetime.datetime.now().strftime("%Y-%m-%d")
	print("語言數", count)
	return langs
