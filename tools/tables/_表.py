#!/usr/bin/env python3

from tables import *
import os, re
import logging
from time import time
from collections import defaultdict
from glob import glob
import inspect
from openpyxl import load_workbook
from xlrd import open_workbook
from docx import Document
from docx.enum.text import WD_UNDERLINE
import regex

logging.basicConfig(format='%(message)s', level=logging.INFO)

YDS = {"+":"又", "-":"白", "*":"俗", "/":"書","\\":"語","=":"文","?":"存疑", "@": "訓"}
def getYD(py):
	return YDS.get(py[-1], "")

def getCompatibilityVariants():
	d = dict()
	for 行 in open("../app/src/main/res/raw/orthography_hz_compatibility.txt",encoding="U8"):
		字, val = 行.rstrip()
		d[字] = val
	return d

def getTsvName(xls):
	name = os.path.basename(xls)
	name = re.sub(r" ?(\(\d{0,3}\))+$", "", name.rsplit(".", 1)[0]) + ".tsv"
	return os.path.join(PATH, SOURCE, name)

def isXlsx(fname):
	return fname.endswith("xlsx")

def isXls(fname):
	return fname.endswith("xls") or fname.endswith("xlsx")

def processFs(v):
	t = type(v)
	if t is float or t is int: return "%d" % v
	if v is None: return ""
	return str(v).strip().replace("\t", " ").replace("\n", " ")

def processXlsxFs(v):
	t = type(v)
	if t is float or t is int: return "%d" % v
	if v is None: return ""
	if t is str: return str(v).strip().replace("\t", " ").replace("\n", " ")
	cells = []
	for i in v:
		if type(i) is str:
			cells.append(i.strip())
			continue
		if type(i) is int or type(i) is float:
			cells.append("%d" % i)
			continue
		text = i.text
		tag = ""
		if i.font.underline == "single":
			tag = "-"
		elif i.font.underline == "double":
			tag = "="
		if tag:
			text = "".join([j + tag for j in text])
		if i.font.vertAlign == "subscript":
			text = f"({text})"
		cells.append(text)
	return "".join(cells).replace(")(", "").strip()

def getXlsxLines(xls, page=0):
	wb = load_workbook(xls, data_only=True, rich_text=True)
	sheet = wb.worksheets[page]
	lines = list()
	for row in sheet.rows:
		列 = [processXlsxFs(j.value) for j in row[:50]]
		if any(列):
			行 = "\t".join(列) + "\n"
			lines.append(行)
	return lines

def getXlsLines(xls, page=0):
	wb = open_workbook(xls)
	sheet = wb.sheet_by_index(page)
	lines = list()
	for i in range(sheet.nrows):
		列 = sheet.row_values(i)
		列 = [processFs(j) for j in 列]
		if any(列):
			行 = "\t".join(列) + "\n"
			lines.append(行)
	return lines

def xls2tsv(xls, page=0):
	tsv = getTsvName(xls)
	if not os.path.exists(xls): return
	if os.path.exists(tsv):
		xtime = os.path.getmtime(xls)
		ttime = os.path.getmtime(tsv)
		if ttime >= xtime: return
	lines = getXlsxLines(xls, page) if isXlsx(xls) else getXlsLines(xls, page)
	t = open(tsv, "w", encoding="U8", newline="\n")
	t.writelines(lines)
	t.close()

def run2text(run):
	tag = ""
	if run.font.underline == WD_UNDERLINE.SINGLE:
		tag = "-"
	elif run.font.underline == WD_UNDERLINE.DOUBLE:
		tag = "="
	elif run.font.underline == WD_UNDERLINE.WAVY:
		tag = chr(0x1AB6)
	elif run._r.xpath("*/w:em[@w:val='dot']"):
		tag = chr(0x0323)
	text = run.text
	if tag:
		text = "".join([i + tag for i in text])
	if run.font.subscript or (run.font.size and run.font.size < 115000):
		text = f"{{{text}}}"
	return text

def isDocx(fname):
	return fname.endswith("docx")
	
def docx2tsv(doc):
	tsv = getTsvName(doc)
	if not os.path.exists(doc): return
	if os.path.exists(tsv):
		xtime = os.path.getmtime(doc)
		ttime = os.path.getmtime(tsv)
		if ttime >= xtime: return
	lines = []
	for each in Document(doc).paragraphs:
		行 = "".join(map(run2text, each.runs)).replace("}{", "")
		lines.append(行 + "\n")
	t = open(tsv, "w", encoding="U8", newline="\n")
	t.writelines(lines)
	t.close()

def ybKey(x):
	if "\t" not in x:
		return x[-1]
	音, js = x.split("\t", 1)
	if js: js = js[0]
	return js + 音[-1]

class 表:
	_time = os.path.getmtime(__file__)
	_file = None
	_files = None
	_sep = None
	顏色 = "#1E90FF"
	全稱 = ""
	簡稱 = ""
	說明 = ""
	網站 = ""
	網址 = ""
	字書 = False

	註序 = False
	patches = None
	ybTrimSpace = True
	kCompatibilityVariants = getCompatibilityVariants()
	simplified = 1
	爲音 = True
	音列 = None
	音典 = defaultdict(set)
	d = defaultdict(list)
	__mod = None
	錯誤 = []
	音集 = set()

	def __init__(自):
		自.錯誤.clear()
		自.音集.clear()

	def setmod(自, mod):
		自.__mod = mod

	def __str__(自):
		if 自.__mod: return 自.__mod
		return 自.__module__.split(".")[-1]

	def find(自, name):
		if os.sep not in name and (isXls(name) or isDocx(name)):
			name = 自.toolname(name)
			if g := 自.find(name): return g
		if os.sep not in name:
			name = 自.全路徑(name)
		if g := glob(name): return g
		if g := glob(re.sub(".([^.]+)$", "([0-9]).\\1", name)): return g
		if g := glob(re.sub(".([^.]+)$", " ([0-9]).\\1", name)): return g
		if isXls(name) or isDocx(name):
			自._file = getTsvName(自._file)
			return 自.find(自._file)
		return

	@property
	def spath(自):
		if 自._files:
			自._file = 自._files[0]
		sname = 自._file
		if not 自.簡稱: 自.簡稱 = 自.info["簡稱"]
		if not 自.簡稱: 自.簡稱 = str(自)
		if not sname: sname = f"{自.簡稱}.tsv"
		g = 自.find(sname)
		if not g or len(g) != 1:
			logging.error(f"\t\t\t{sname}查找結果：{g}")
			return
		sname = g[0]
		自._file = os.path.basename(sname)
		if isXls(sname):
			page = 1 if 自.簡稱 in ("中山石岐", "通城大坪", "1796建甌") else 0
			if 自.簡稱 == "開平護龍": page = 3
			xls2tsv(sname, page)
			sname = getTsvName(sname)
		elif isDocx(sname):
			docx2tsv(sname)
			sname = getTsvName(sname)
		return sname

	def toolname(自, name):
		name = os.path.basename(name)
		return os.path.join(PATH, "..", name)

	def 全路徑(自, name):
		name = os.path.basename(name)
		return os.path.join(PATH, SOURCE, name)

	@property
	def tpath(自):
		tpath = os.path.join(PATH, TARGET, str(自))
		if not tpath.endswith(".tsv"): tpath += ".tsv"
		return tpath

	def normS(自, s, rep="[\\1]"):
		s = s.replace("(", "（").replace(")", "）")
		s = regex.sub("（((?>[^（）]+|(?R))*)）", rep, s)
		return s

	def normM(自, s, rep="〚\\1〛"):
		s = s.replace("[", "［").replace("]", "］")
		s = regex.sub("［((?>[^［］]+|(?R))*)］", rep, s)
		return s

	def normG(自, s, rep="｛\\1｝"):
		s = s.replace("｛", "{").replace("｝", "}")
		s = regex.sub(r"\{((?>[^\{\}]+|(?R))*)\}", rep, s)
		return s

	def 爲舊(自):
		classfile = inspect.getfile(自.__class__)
		classtime = os.path.getmtime(classfile)
		varianttime = os.path.getmtime(VARIANT_FILE)
		if classtime < varianttime:
			classtime = varianttime
		spath = 自.spath
		if not spath or not os.path.exists(spath):
			return False
		if os.path.exists(自.tpath):
			ftime = os.path.getmtime(spath)
			ttime = os.path.getmtime(自.tpath)
			if ttime < 自._time: return True
			if ttime < classtime: return True
			return ttime < ftime
		return True

	def patch(自, d):
		if not 自.patches: return
		for 字, py in 自.patches.items():
			if not py:
				del d[字]
				continue
			d[字] = py.split(",")

	def normAll(自, 音):
		音 = 音.replace("᷉", "̃").replace("ⱼ", "ᶽ")\
			.replace("ʦ", "ts").replace("ʨ", "tɕ").replace("ʧ", "tʃ")\
			.replace("ʣ", "dz").replace("ʥ", "dʑ")\
			.replace("", "ᵑ").replace("", "ᶽ")
		return 音

	def 正音(自, 音):
		if 自.爲語() and 自.爲音:
			音 = 音.strip()
			音 = 音.replace("Ǿ", "Ǿ").replace("Ǿ", "").lstrip("∅︀0∅Ø〇").replace("零", "")
			if 音.startswith("I") or 音.startswith("1"): 音 = "l" + 音[1:]
			音 = 音.lower().replace("g", "ɡ").replace("ʼ", "ʰ").replace("'", "ʰ").replace("‘", "ʰ")
			if not 音.startswith("h") and "h" in 音:
				音 = 音.replace("h", "ʰ")
			if 自.ybTrimSpace:
				音 = 音.replace(" ", "")
			音 = 音.replace("[", "").replace("]", "")
			音 = re.sub(r"^([mnvʋɹl])(\d+)$", "\\1\u0329\\2", 音)
			音 = re.sub(r"^([ŋȵʐɱɻʒ])(\d+)$", "\\1\u030D\\2", 音)
			if 自.info["無調"]:
				音 = 音.rstrip("0123456789")
		return 音

	def checkYb(自, 音):
		音 = 自.正音(音)
		if "\t" in 音:
			自.錯誤.append(f"{音} 音節有TAB空檔")
			音 = 音.replace("\t", "")
		if isHZ(音[0]):
			自.錯誤.append(f"{音} 音節錯誤")
		if re.match(r".+\d{3,}", 音):
			自.錯誤.append(f"{音} 調類錯誤")
		if 音 not in 自.音集:
			自.音集.add(音)
		else:
			自.錯誤.append(f"{音} 音節重複")
		return 音

	def 爲方言(自):
		return str(自) in ("老國音","黨項") or (自.langType and not 自.langType.startswith("歷史音"))

	def normJS(自, js):
		if not js: return ""
		last = ""
		l = list()
		for i in js:
			if isHZ(i):
				if last: l.append(last)
				last = ""
				l.append(i)
			else:
				last += i
		if last: l.append(last)
		return " ".join(l)

	def normPart(自, js):
		if not js: return ""
		last = ""
		l = list()
		for i in js:
			if len(i.encode()) > 1:
				if last: l.append(last)
				last = ""
				l.append(i)
			else:
				last += i
		if last: l.append(last)
		return " ".join(l)

	def 寫(自, d):
		自.patch(d)
		t = open(自.tpath, "w", encoding="U8", newline="\n")
		print(f"#漢字\t音標\t解釋", file=t)
		for 字 in sorted(d.keys()):
			pys = d[字]
			字 = 自.kCompatibilityVariants.get(字, 字)
			if 自.爲方言() and 自.simplified:
				字 = s2t(字, 自.simplified)
			if not isHZ(字):
				if 自.爲方言():
					自.錯誤.append(f"【{字}】不是漢字，讀音爲：{','.join([i.strip() for i in pys])}")
				continue
			if 自.註序:
				pys = sorted(pys,key=ybKey)
			for py in pys:
				if "\t" in py:
					音, js = py.split("\t", 1)
					js = js.strip().replace("~", "～").replace("...", "⋯").replace("∽", "～")
				else:
					音, js = py, ""
				音 = 自.正音(音)
				音 = f"{音}\t{js}"
				音 = 自.normAll(音)
				print(f"{字}\t{音}", file=t)
		t.close()

	@property
	def langType(自):
		return 自.info["地圖集二分區"]

	def 爲語(自):
		return 自.langType != None

	@property
	def count(自):
		return len(自.d) + 自.unknownCount - (1 if 自.unknownCount > 0 else 0)
	
	@property
	def unknownCount(自):
		n = len(自.d.get("□", []))
		if 自.爲語():
			return n
		else:
			return 1 if n > 0 else 0

	@property
	def 聲韻調數(自):
		return len(自.音典)

	@property
	def 聲韻數(自):
		return len(set(map(lambda x:x.split("/")[0].rstrip("1234567890"), 自.音典.keys())))

	def 讀(自):
		start = time()
		if 自.爲舊(): 自.更新()
		自.音典.clear()
		自.d.clear()
		if not 自.tpath or not os.path.exists(自.tpath): return
		for 行 in open(自.tpath,encoding="U8"):
			行 = 行.strip()
			if 行.startswith("#"): continue
			if "\t" not in 行: continue
			字, py = 行.split("\t", 1)
			if 自.爲語():
				js = ""
				if "\t" in py: py, js = py.split("\t", 1)
				if js and 自.爲語():
					js = 自.normJS(js)
				try:
					yd = getYD(py)
				except:
					print("\t\t\t", 自.簡稱, py, js)
					exit(1)
				if yd and py.count("*") <= 1:
					js = f"({yd}){js}"
					py = py[:-1]
				if re.match(r"^\([^()]*?\)$", js):
					js = js[1:-1]
				syd = re.sub(r"\(.*?\)","",py).strip(" _`*")
				if "-" not in syd:
					自.音典[syd].add(字)
				if js:
					py += "{%s}" % js
			else:
				if 自.字書:
					sep = "▲" if str(自) == "匯纂" else "\t"
					py2, js = py.split(sep, 1)
					py = ("\n\n" if 自.d[字] else "") + py2 + sep + 自.normJS(js)
				elif 自.簡稱 in ("部件檢索","字形描述"):
					py = 自.normPart(py)
				py = py.replace("\t", "\n")
			if py not in 自.d[字]:
				自.d[字].append(py)
		# passed = time() - start
		# logging.info(f"({自.count:5d}({自.unknownCount})-{自.聲韻調數:4d}-{自.聲韻數:4d}) {passed:6.3f} {自}")
	
	def load(自, dicts):
		自.讀()
		if not 自.d: return
		for 字, 音集 in 自.d.items():
			if 字 not in dicts:
				dicts[字] = {"漢字": 字}
			dicts[字][str(自)] = "\t".join(音集)
	
	def 析(自, 列):
		return tuple(列[:3])

	def 統(自, 行):
		行 = 行.replace(" ", " ")
		return 行
	
	@property
	def sep(自):
		if 自._sep: return 自._sep
		sep = "\t"
		spath = 自.spath
		if spath.endswith(".csv"): sep = ","
		elif spath.endswith(".tsv"): sep = "\t"
		elif spath.endswith(".txt"): sep = " "
		return sep

	def 更新(自):
		d = defaultdict(list)
		sep = 自.sep
		skip = 自.info.get("跳過行數", 0)
		lineno = 0
		files = 自._files if 自._files else [自.spath]
		for spath in files:
			for 行 in open(自.全路徑(spath),encoding="U8"):
				lineno += 1
				if lineno <= skip: continue
				行 = 自.統(行)
				if 行.startswith('#') : continue
				列 = [i.strip() for i in 行.strip('\n').split(sep)]
				entries = 自.析(列)
				if not entries: continue
				if type(entries) is tuple: entries = [entries]
				for 列 in entries:
					if len(列) <= 1: continue
					if len(列) >= 2:
						字, 音 = 列[:2]
						js = "\t".join(列[2:])
					if not 字 or len(字) != 1: continue
					if not 音: continue
					if 自.爲方言():
						if isHZ(音[0]): continue
					p = f"{音}\t{js}"
					p = p.strip()
					if p not in d[字]:
						d[字].append(p)
		自.寫(d)

	def splitSySd(自, syd):
		if not syd: return "",""
		tonesymbol = "⁰¹²³⁴⁵⁶"
		tonemark = "˩˨˧˦˥"
		for i in tonesymbol:
			syd = syd.replace(i, str(tonesymbol.index(i)))
		for i in tonemark:
			syd = syd.replace(i, str(tonemark.index(i)+1))
		sy = syd.rstrip("-0123456789")
		sd = syd[len(sy):]
		return sy,sd

	def dz2dl(自, sy, dz=None):
		sy = sy.strip()
		if dz is None:
			if "/" in sy:
				return "/".join(map(自.dz2dl, sy.split("/")))
			sy,dz = 自.splitSySd(sy)
		if not dz: return sy
		dl = 自.dz2dlWithYm(dz, sy)
		return sy + dl

	def dz2dlWithYm(自, dz, sy):
		dl = ""
		if dz not in 自.toneMaps:
			if dz == "0":
				dl = dz
			elif len(dz) == 1:
				dz = dz + dz
				if dz in 自.toneMaps:
					dl = 自.toneMaps[dz]
			else:
				dl = "?"
		else:
			dl = 自.toneMaps[dz]
		if sy and sy[-1] in "ptkʔ̚" and dz + "0" in 自.toneMaps:
			dl = 自.toneMaps[dz + "0"]
		return dl
