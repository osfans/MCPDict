#!/usr/bin/env python3

from tables import *
import os, re
import logging
from collections import defaultdict, OrderedDict
from glob import glob
import inspect
from openpyxl import load_workbook
from xlrd import open_workbook
import docx
from docx import Document
from docx.table import Table
from docx.text.paragraph import Paragraph
from docx.shared import Pt
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
		if i.font.vertAlign == "subscript" or (i.font.size and i.font.size < 10.0):
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
	if run.font.subscript or (run.font.size and run.font.size < Pt(9)):
		if text.startswith("{") and text.endswith("}"):
			pass
		elif text.startswith("[") and text.endswith("]"):
			pass
		else:
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
	Doc = Document(doc)
	Doc.paragraphs
	for each in Doc._body._element:
		if isinstance(each, docx.oxml.table.CT_Tbl):
			t = Table(each, Doc)
			for row in t.rows:
				行 = ""
				for cell in row.cells:
					for p in cell.paragraphs:
						行 += "".join(map(run2text, p.runs)).replace("\t", "").replace("\n", "")
					行 += "\t"
				lines.append(行.replace("}{", "").strip())
		elif isinstance(each, docx.oxml.text.paragraph.CT_P):
			element = Paragraph(each, Doc)
			行 = "".join(map(run2text, element.runs)).replace("}{", "")
			lines.append(行)
	行 = "\n".join(lines).replace("}\n{", "")
	t = open(tsv, "w", encoding="U8", newline="\n")
	t.write(行)
	t.close()

def ybKey(x):
	if "\t" not in x:
		return x[-1]
	音, 註 = x.split("\t", 1)
	if 註: 註 = 註[0]
	return 註 + 音[-1]

class 表:
	_time = os.path.getmtime(__file__)
	文件名 = None
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
	補丁 = None
	kCompatibilityVariants = getCompatibilityVariants()
	simplified = 1
	爲音 = True
	音列 = None
	音典 = defaultdict(set)
	音表 = OrderedDict()
	聲韻典 = defaultdict(set)
	d = defaultdict(list)
	__mod = None
	誤 = []
	音集 = set()

	def __init__(自):
		自.誤.clear()
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
			自.文件名 = getTsvName(自.文件名)
			return 自.find(自.文件名)
		return

	@property
	def spath(自):
		if 自._files:
			自.文件名 = 自._files[0]
		sname = 自.文件名
		if not 自.簡稱: 自.簡稱 = 自.info["簡稱"]
		if not 自.簡稱: 自.簡稱 = str(自)
		if not sname: sname = f"{自.簡稱}.tsv"
		g = 自.find(sname)
		if not g:
			logging.error(f"\t\t未找到 {sname}")
			return
		if len(g) != 1:
			logging.error(f"\t\t找到多个 {sname}：{g}")
			return
		sname = g[0]
		自.文件名 = os.path.basename(sname)
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
		tpath = os.path.join(PATH, TARGET, 自.簡稱)
		if not tpath.endswith(".tsv"): tpath += ".tsv"
		return tpath

	def normS(自, s, rep="｛\\1｝"):
		s = regex.sub(r"\(((?>[^\(\)]+|(?R))*)\)", rep, s)
		return s

	def normM(自, s, rep="｛\\1｝"):
		s = regex.sub(r"\[((?>[^\[\]]+|(?R))*)\]", rep, s)
		return s

	def normG(自, s, rep="｛\\1｝"):
		s = regex.sub(r"\{((?>[^\{\}]+|(?R))*)\}", rep, s)
		return s

	def 過時(自):
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

	def 修訂(自, d):
		if not 自.補丁: return
		for 字, 音 in 自.補丁.items():
			if not 音:
				del d[字]
				continue
			d[字] = 音.split(",")

	def _正音(自, 音):
		if 自.爲語() and 自.爲音:
			音 = 音.strip()
			音 = 音.replace("Ǿ", "Ǿ").replace("Ǿ", "").lstrip("∅︀∅Ø〇0").replace("零", "")
			if 自.簡稱 not in ("盛唐", "榕江侗上古借詞", "榕江侗中古借詞") and not 自.文件名.startswith("白語"): 音 = 音.lstrip("q")
			if 音.startswith("I") or 音.startswith("1"): 音 = "l" + 音[1:]
			音 = 音.lower().replace("g", "ɡ").replace("ʼ", "ʰ").replace("'", "ʰ").replace("‘", "ʰ")
			if not 音.startswith("h") and "h" in 音:
				音 = 音.replace("h", "ʰ")
			音 = 音.replace(" ", "")
			音 = 音.replace("[", "").replace("]", "").replace("{", "").replace("}", "")
			音 = re.sub(r"^([mnvʋɹl])(\d+)$", "\\1\u0329\\2", 音)
			音 = re.sub(r"^([ŋȵʐɱɻʒ])(\d+)$", "\\1\u030D\\2", 音)
			if 自.info["無調"]:
				音 = 音.rstrip("0123456789")
		return 音

	def 正音(自, 音, 檢查=False):
		音 = 自._正音(音)
		if not 檢查: return 音
		if "\t" in 音:
			自.誤.append(f"[{音}]音節含TAB字符")
			音 = 音.replace("\t", "")
		if not re.match(r".+\d{0,2}[a-z\-=]?", 音):
			自.誤.append(f"[{音}]音節錯誤")
		elif 有字(音):
			自.誤.append(f"[{音}]音節包含漢字")
		if 音 not in 自.音集:
			自.音集.add(音)
		else:
			自.誤.append(f"[{音}]音節重複")
		return 音

	def 檢查同音字(自):
		return 自.分區 and 自.簡稱 not in ("普通話",) and not 自.分區.startswith("歷史音") and not 自.分區.startswith("域外方音")

	def 爲方言(自):
		return 自.簡稱 in ("老國音","党項") or (自.爲語() and not 自.分區.startswith("歷史音"))

	def 分註(自, 註):
		if not 註: return ""
		上 = ""
		果 = list()
		for 字 in 註:
			if 爲字(字):
				if 上: 果.append(上)
				上 = ""
				果.append(字)
			else:
				上 += 字
		if 上: 果.append(上)
		return " ".join(果)

	def 正部件(自, 註):
		if not 註: return ""
		上 = ""
		果 = list()
		for 部件 in 註:
			if len(部件.encode()) > 1:
				if 上: 果.append(上)
				上 = ""
				果.append(部件)
			else:
				上 += 部件
		if 上: 果.append(上)
		return " ".join(果)

	def 寫(自, d):
		自.修訂(d)
		t = open(自.tpath, "w", encoding="U8", newline="\n")
		print(f"#漢字\t音標\t解釋", file=t)
		for 字 in sorted(d.keys()):
			pys = d[字]
			字 = 自.kCompatibilityVariants.get(字, 字)
			if 自.爲方言() and 自.simplified:
				字 = s2t(字, 自.simplified)
			if not 爲字(字):
				if 自.爲方言():
					自.誤.append(f"【{字}】({','.join([i.strip() for i in pys])})不是漢字")
				continue
			if 自.註序:
				pys = sorted(pys,key=ybKey)
			for py in pys:
				if "\t" in py:
					音, 註 = py.split("\t", 1)
					註 = 註.strip()
				else:
					音, 註 = py, ""
				音 = 自.正音(音)
				if 字 == "□" and not 註:
					自.誤.append(f"【□】({音})無註釋")
				音 = f"{音}\t{註}"
				print(f"{字}\t{音}", file=t)
		t.close()

	@property
	def 分區(自):
		return 自.info["地圖集二分區"]

	def 爲語(自):
		return 自.分區 != None

	@property
	def 字數(自):
		return len(自.d) + 自.框數 - (1 if 自.框數 > 0 else 0)
	
	@property
	def 框數(自):
		數 = len(自.d.get("□", []))
		if 自.爲語(): return 數
		return 1 if 數 > 0 else 0

	@property
	def 音節數(自):
		return len(自.音典)

	@property
	def 聲韻數(自):
		return len(自.聲韻典)

	def 讀(自, 更新=False):
		自.音表.clear()
		自.音典.clear()
		自.聲韻典.clear()
		自.d.clear()
		if 自.過時() or 更新 and 自.spath: 自.更新()
		if not 自.tpath or not os.path.exists(自.tpath): return
		for 行 in open(自.tpath,encoding="U8"):
			行 = 行.strip()
			if 行.startswith("#"): continue
			if "\t" not in 行: continue
			字, py = 行.split("\t", 1)
			if 自.爲語():
				註 = ""
				if "\t" in py: py, 註 = py.split("\t", 1)
				if 註 and 自.爲語():
					註 = 自.分註(註)
				try:
					異讀 = getYD(py)
				except:
					print("\t\t\t", 自.簡稱, py, 註)
					exit(1)
				if 異讀 and py.count("*") <= 1:
					註 = f"({異讀}){註}"
					py = py[:-1]
				if re.match(r"^\([^()]*?\)$", 註):
					註 = 註[1:-1]
				音 = re.sub(r"\(.*?\)","",py).strip(" _`*")
				音 = 音.split("/", 1)[0]
				if "-" not in 音:
					自.音典[音].add(字)
					繁註 = s2t(註.replace(" ", ""))
					if "訓" not in 繁註 and "替" not in 繁註 and "口語" not in 繁註 and "合音" not in 繁註 and "語流" not in 繁註 and "音變" not in 繁註 and "連讀" not in 繁註 and "存疑" not in 繁註 and "地方字" not in 繁註 and "地名" not in 繁註 and "俗" not in 繁註 and 字 != "□":
						聲韻 = 自.分音(音)[0]
						自.聲韻典[聲韻].add(字)
				if 註:
					py += "{%s}" % 註
			else:
				if 自.字書:
					sep = "▲" if 自.簡稱 == "匯纂" else "\t"
					py2, 註 = py.split(sep, 1)
					py = ("\n\n" if 自.d[字] else "") + py2 + sep + 自.分註(註)
				elif 自.簡稱 in ("部件檢索","字形描述"):
					py = 自.正部件(py)
				py = py.replace("\t", "\n")
			if py not in 自.d[字]:
				自.d[字].append(py)
	
	def 加載(自, dicts, 更新=False):
		自.讀(更新)
		if not 自.d: return
		for 字, 音集 in 自.d.items():
			if 字 not in dicts:
				dicts[字] = {"漢字": 字}
			dicts[字][自.簡稱] = "\t".join(音集)
	
	def 析(自, 列):
		return tuple(列[:3])

	def 統(自, 行):
		if not 自.爲方言(): return 行
		for i in range(1, 10):
			sda = chr(ord('➀') + (i - 1))
			sdb = chr(ord('①') + (i - 1))
			行 = 行.replace(sda, sdb)
		for i in range(0, 10):
			sda = chr(ord('₀') + i)
			sdb = chr(ord('0') + i)
			行 = 行.replace(sda, sdb)
			sda = chr(ord('０') + i)
			行 = 行.replace(sda, sdb)
		行 = 行.replace(" ", " ")\
			.replace("（", "(").replace("）", ")")\
			.replace("［", "[").replace("］", "]")\
			.replace("｛", "{").replace("｝", "}")\
			.replace("／", "/").replace("？", "?").replace("！", "!").replace("：", ":").replace("；",";").replace("...", "⋯").replace("｜", "|")\
			.replace("∽", "~").replace("～", "~")
		行 = 行.replace("\u1dc9", "\u0303")\
			.replace("ʦ", "ts").replace("ʨ", "tɕ").replace("ʧ", "tʃ")\
			.replace("ʣ", "dz").replace("ʥ", "dʑ")\
			.replace("", "䝼")\
			.replace("", "ᵑ").replace("", "ᶽ")#.replace("ⱼ", "ᶽ")
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
						if 爲字(音[0]): continue
					p = f"{音}\t{js}"
					p = p.strip()
					if p not in d[字]:
						d[字].append(p)
		自.寫(d)

	def 分音(自, 音):
		if not 音: return "",""
		調值 = "⁰¹²³⁴⁵⁶"
		調號 = "˩˨˧˦˥"
		for i in 調值:
			音 = 音.replace(i, str(調值.index(i)))
		for i in 調號:
			音 = 音.replace(i, str(調號.index(i)+1))
		聲韻 = re.split(r"\d", 音, maxsplit=1)[0]
		調 = 音[len(聲韻):]
		return 聲韻,調

	def 轉調類(自, 音):
		音 = 音.strip()
		if "/" in 音:
			return "/".join(map(自.轉調類, 音.split("/")))
		if "-" in 音:
			return "-".join(map(自.轉調類, 音.split("-")))
		聲韻,調值 = 自.分音(音)
		if not 調值: return 聲韻
		調類 = 自.僅轉調類(調值, 聲韻)
		return 聲韻 + 調類

	def 僅轉調類(自, 調值, 聲韻=""):
		調類 = ""
		if 調值 not in 自.調典:
			if 調值 == "0":
				調類 = 調值
			elif len(調值) == 1:
				調值 = 調值 + 調值
				if 調值 in 自.調典:
					調類 = 自.調典[調值]
			else:
				調類 = ""
		else:
			調類 = 自.調典[調值]
		if 聲韻 and 聲韻[-1] in "ptkʔ̚" and 調值 + "0" in 自.調典:
			調類 = 自.調典[調值 + "0"]
		return 調類
