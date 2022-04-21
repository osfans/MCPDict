#!/usr/bin/env python3

from tables import *
import os, re
import logging
from time import time
from collections import defaultdict
from glob import glob
import inspect
from openpyxl import load_workbook

logging.basicConfig(format='%(message)s', level=logging.INFO)

SOURCE = "data"
TARGET = "output"
VARIANT_FILE = f"tables/{SOURCE}/正字.tsv"

YDS = {"+":"又","-":"白","*":"俗", "/":"書","\\":"語","=":"文","~":"訓","≈":"替", "?":"存疑"}
def getYD(py):
	return YDS.get(py[-1], "")

def isHZ(c):
	c = c.strip()
	if len(c) != 1: return False
	n = ord(c)
	return 0x3400<=n<0xA000 or n in (0x25A1, 0x3007) or 0xF900<=n<0xFB00 or 0x20000<=n<0x31350

def getCompatibilityVariants():
	d = dict()
	for line in open("../app/src/main/res/raw/orthography_hz_compatibility.txt",encoding="U8"):
		hz, val = line.rstrip()
		d[hz] = val
	return d

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

def getTsvName(xls):
	return re.sub("\.xlsx?$", ".tsv", xls)

def isXls(fname):
	return fname.endswith(".xls") or fname.endswith("xlsx")

def xls2tsv(xls):
	tsv = getTsvName(xls)
	if not os.path.exists(xls): return
	if os.path.exists(tsv):
		xtime = os.path.getmtime(xls)
		ttime = os.path.getmtime(tsv)
		if ttime >= xtime: return
	wb = load_workbook(xls)
	sheet = wb.worksheets[0]
	lines = list()
	for row in sheet.rows:
		fs = [(str(int(j.value)) if type(j.value) is float else str(j.value).strip()) if j.value else "" for j in row[:10]]
		if any(fs):
			line = "\t".join(fs) + "\n"
			lines.append(line)
	t = open(tsv, "w", encoding="U8")
	t.writelines(lines)
	t.close()

class 表:
	path = os.path.dirname(os.path.abspath(__file__))
	_time = os.path.getmtime(__file__)
	_file = None
	_sep = None
	color = "#1E90FF"
	short = ""
	note = ""
	site = ""
	url = ""

	disorder = False
	patches = None
	ybTrimSpace = True
	kCompatibilityVariants = getCompatibilityVariants()
	simplified = 1
	normVariants = getSTVariants(1)
	stVariants = getSTVariants(2)
	isYb = True
	syds = defaultdict(set)
	d = defaultdict(list)
	__mod = None

	def setmod(self, mod):
		self.__mod = mod

	def __str__(self):
		if self.__mod: return self.__mod
		return self.__module__.split(".")[-1]

	@property
	def spath(self):
		sname = self._file
		if not self.short: self.short = self.info["簡稱"]
		if not self.short: self.short = str(self)
		if not sname: sname = f"{self.short}.tsv"
		if not sname.startswith("/"):
			sname = self.get_fullname(sname)
		g = glob(sname)
		if not g or len(g) != 1:
			if isXls(sname):
				self._file = getTsvName(self._file)
				return self.spath
			logging.error(f"\t\t\t{sname} {g}")
			return
		sname = g[0]
		if isXls(sname):
			xls2tsv(sname)
			sname = getTsvName(sname)
		self._file = os.path.basename(sname)
		return sname

	def get_fullname(self, name):
		return os.path.join(self.path, SOURCE, name)

	@property
	def tpath(self):
		tpath = os.path.join(self.path, TARGET, str(self))
		if not tpath.endswith(".tsv"): tpath += ".tsv"
		return tpath

	def outdated(self):
		classfile = inspect.getfile(self.__class__)
		classtime = os.path.getmtime(classfile)
		varianttime = os.path.getmtime(VARIANT_FILE)
		if classtime < varianttime:
			classtime = varianttime
		if not self.spath or not os.path.exists(self.spath):
			return False
		if os.path.exists(self.tpath):
			ftime = os.path.getmtime(self.spath)
			ttime = os.path.getmtime(self.tpath)
			if ttime < self._time: return True
			if ttime < classtime: return True
			return ttime < ftime
		return True

	def patch(self, d):
		if not self.patches: return
		for hz, py in self.patches.items():
			if not py:
				del d[hz]
				continue
			d[hz] = py.split(",")

	def normAll(self, yb):
		yb = yb.replace("᷉", "̃").replace("ⱼ", "ᶽ")\
			.replace("ʦ", "ts").replace("ʨ", "tɕ").replace("ʧ", "tʃ")\
			.replace("ʣ", "dz").replace("ʥ", "dʑ")\
			.replace("", "ᵑ").replace("", "ᶽ")
		return yb

	def normYb(self, yb):
		if self.isLang() and self.isYb:
			yb = yb.strip()
			yb = yb.replace("Ǿ", "Ǿ").replace("Ǿ", "ˀ").lstrip("0∅Ø零")
			yb = yb.lower().replace("g", "ɡ").replace("ʼ", "ʰ")
			if not yb.startswith("h") and "h" in yb:
				yb = yb.replace("h", "ʰ")
			if self.ybTrimSpace:
				yb = yb.replace(" ", "")
		return yb

	def isDialect(self):
		return self.langType and self.langType not in ("歷史音",)

	def write(self, d):
		self.patch(d)
		t = open(self.tpath, "w",encoding="U8")
		print(f"#漢字\t音標\t解釋", file=t)
		for hz in sorted(d.keys()):
			pys = d[hz]
			hz = self.kCompatibilityVariants.get(hz, hz)
			if self.isDialect() and self.simplified:
				if self.simplified == 1:
					hz = self.normVariants.get(hz, hz)
				else:
					hz = self.stVariants.get(hz, hz)
			if not isHZ(hz):
				if self.isDialect():
					print("\t\t\t", hz, pys)
				continue
			if self.disorder:
				pys = sorted(pys,key=lambda x:x.split("\t", 1)[0][-1])
			for py in pys:
				if "\t" in py:
					yb, js = py.split("\t", 1)
					js = js.strip().replace("~", "～")
				else:
					yb, js = py, ""
				yb = self.normYb(yb)
				yb = f"{yb}\t{js}"
				yb = self.normAll(yb)
				print(f"{hz}\t{yb}", file=t)
		t.close()

	@property
	def langType(self):
		return self.info["地圖集二分區"]

	def isLang(self):
		return self.langType != None

	@property
	def count(self):
		return len(self.d)

	@property
	def sydCount(self):
		return len(self.syds)

	@property
	def syCount(self):
		return len(set(map(lambda x:x.rstrip("1234567890"), self.syds.keys())))

	def read(self):
		start = time()
		if self.outdated(): self.update()
		self.syds.clear()
		self.d.clear()
		if not self.tpath or not os.path.exists(self.tpath): return
		for line in open(self.tpath,encoding="U8"):
			line = line.strip()
			if line.startswith("#"): continue
			if "\t" not in line: continue
			hz, py = line.split("\t", 1)
			if self.isLang():
				js = ""
				if "\t" in py: py, js = py.split("\t", 1)
				yd = getYD(py)
				if yd and py.count("*") <= 1:
					js = f"({yd}){js}"
					py = py[:-1]
				if re.match("^\([^()]*?\)$", js):
					js = js[1:-1]
				syd = re.sub("\(.*?\)","",py).strip(" *|")
				if "-" not in syd:
					self.syds[syd].add(hz)
				if js: py += "{%s}" % js
			else:
				py = py.replace("\t", "\n")
			if py not in self.d[hz]:
				self.d[hz].append(py)
		passed = time() - start
		logging.info(f"({self.count:5d}-{self.sydCount:4d}-{self.syCount:4d}) {passed:6.3f} {self}")
	
	def load(self, dicts):
		self.read()
		if not self.d: return
		for hz, ybs in self.d.items():
			if hz not in dicts:
				dicts[hz] = {"漢字": hz}
			dicts[hz][str(self)] = ",".join(ybs)
	
	def parse(self, fs):
		return tuple(fs[:3])

	def format(self, line):
		return line
	
	@property
	def sep(self):
		if self._sep: return self._sep
		sep = "\t"
		spath = self.spath
		if spath.endswith(".csv"): sep = ","
		elif spath.endswith(".tsv"): sep = "\t"
		elif spath.endswith(".txt"): sep = " "
		return sep

	def update(self):
		d = defaultdict(list)
		sep = self.sep
		skip = self.info.get("跳過行數", 0)
		lineno = 0
		for line in open(self.spath,encoding="U8"):
			lineno += 1
			if lineno <= skip: continue
			if line.startswith('#') or line.startswith('"#') : continue
			line = self.format(line)
			fs = [i.strip('" \t') for i in line.strip('\n').split(sep)]
			entries = self.parse(fs)
			if not entries: continue
			if type(entries) is tuple: entries = [entries]
			for fs in entries:
				if len(fs) <= 1: continue
				if len(fs) >= 2:
					hz, yb = fs[:2]
					js = "\t".join(fs[2:])
				if not hz or len(hz) != 1: continue
				if not yb: continue
				p = f"{yb}\t{js}"
				p = p.strip()
				if p not in d[hz]:
					d[hz].append(p)
		self.write(d)
