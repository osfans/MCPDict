#!/usr/bin/env python3

from tables import *
import os
import logging
from time import time
from collections import defaultdict
from glob import glob
import inspect

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

class 表:
	site = None
	url = None
	path = os.path.dirname(os.path.abspath(__file__))
	_time = os.path.getmtime(__file__)
	_file = None
	_sep = None
	tones = None
	location = None
	size = None
	ver = None
	color = "#1E90FF"
	book = ""
	editor = ""
	note = ""
	
	disorder = False
	hasHead = True
	patches = None
	ybTrimSpace = True
	kCompatibilityVariants = getCompatibilityVariants()
	simplified = 1
	normVariants = getSTVariants(1)
	stVariants = getSTVariants(2)
	isYb = True
	jointer = ","
	count = 0

	@property
	def spath(self):
		sname = self._file
		if not sname: sname = f"{self.city}.tsv"
		if not sname.startswith("/"):
			sname = os.path.join(self.path, SOURCE, sname)
		g = glob(sname)
		if g:
			if len(g) == 1: return g[0]
		logging.error(f"\t\t\t{sname} {g}")

	def get_fullname(self, name):
		return os.path.join(self.path, SOURCE, name)

	@property
	def tpath(self):
		tpath = os.path.join(self.path, TARGET, self.city)
		if not tpath.endswith(".tsv"): tpath += ".tsv"
		return tpath

	@property
	def city(self):
		return self.__module__.split(".")[-1]
	
	def __str__(self):
		return self.key.split(",")[0]

	@property
	def desc(self):
		s = self.note
		if self.book: s = f"參考資料：{self.book}<br>{s}"
		if self.editor: s = f"錄入人：{self.editor}<br>{s}"
		if self.ver: s = f"版本：{self.ver}<br>{s}"
		if self.count > 0: s = "字數：%d<br><br>%s"%(self.count, s)
		return s.replace("\n", "")

	@property
	def head(self):
		return str(self), self.lang, self.city, self.color, self.site, self.url, self.desc, self.tones, self.location, self.size

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
			.replace("ʣ", "dz").replace("ʥ", "dʑ")
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

	def isOldChinese(self):
		return self.key.startswith("ltc_") or self.key.startswith("och_")

	def isForeign(self):
		return self.key.startswith("vi_") or self.key.startswith("ko_") or self.key.startswith("ja_")

	def isDialect(self):
		return self.isLang() and not self.isForeign() and not self.isOldChinese()

	def write(self, d):
		self.patch(d)
		t = open(self.tpath, "w",encoding="U8")
		print(f"#漢字\t音標\t解釋", file=t)
		print(f"#簡稱\t/\t{self.city}", file=t)
		print(f"#全稱\t/\t{self.lang}", file=t)
		print(f"#聲母\t/\t", file=t)
		print(f"#韻母\t/\t", file=t)
		print(f"#聲調\t/\t{self.tones}", file=t)
		print(f"#版本\t/\t{self.ver}", file=t)
		print(f"#錄入人\t/\t{self.editor}", file=t)
		print(f"#參考資料\t/\t{self.book}", file=t)
		print(f"#說明\t/\t{self.note}", file=t)
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

	def isLang(self):
		return "_" in str(self)

	def isDict(self):
		return not self.isLang()

	def read(self):
		start = time()
		if self.outdated(): self.update()
		d = defaultdict(list)
		if not self.tpath or not os.path.exists(self.tpath): return
		for line in open(self.tpath,encoding="U8"):
			line = line.strip()
			if line.startswith("#"): continue
			if "\t" not in line: continue
			hz, py = line.split("\t", 1)
			if self.isDict():
				py = py.replace("\t", "\n")
			else:
				js = ""
				if "\t" in py: py, js = py.split("\t", 1)
				yd = getYD(py)
				if yd and py.count("*") <= 1:
					js = f"({yd}){js}"
					py = py[:-1]
				if js.startswith("(") and js.endswith(")"):
					js = js[1:-1]
				if js: py += "{%s}" % js
			if py not in d[hz]:
				d[hz].append(py)
		passed = time() - start
		self.count = len(d)
		logging.info(f"({self.count:5d}) {passed:5.3f} {self.city}")
		return d
	
	def load(self, dicts):
		d = self.read()
		if not d: return
		for hz, ybs in d.items():
			if hz not in dicts:
				dicts[hz] = {"hz": hz}
			dicts[hz][str(self)] = self.jointer.join(ybs)
	
	def parse(self, fs):
		return tuple(fs[:3])

	def format(self, line):
		return line
	
	@property
	def sep(self):
		if self._sep: return self._sep
		sep = "\t"
		if self.spath.endswith(".csv"): sep = ","
		elif self.spath.endswith(".tsv"): sep = "\t"
		elif self.spath.endswith(".txt"): sep = " "
		return sep

	def update(self):
		d = defaultdict(list)
		firstline = False
		
		for line in open(self.spath,encoding="U8"):
			if self.hasHead and not firstline:
				firstline = True
				continue
			if line.startswith('#') or line.startswith('"#') : continue
			line = self.format(line)
			fs = [i.strip('" \t') for i in line.strip('\n').split(self.sep)]
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
