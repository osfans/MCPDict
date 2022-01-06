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

YDS = {"+":"又","-":"白","*":"俗", "/":"書","\\":"語","=":"文","~":"訓","≈":"替", "?":"存疑",}
def getYD(py):
	return YDS.get(py[-1], "")

def isHZ(c):
	c = c.strip()
	if len(c) != 1: return False
	n = ord(c)
	return 0x3400<=n<0xA000 or n in (0x25A1, 0x3007) or 0xF900<=n<0xFB00 or 0x20000<=n<0x31350

def getCompatibilityVariants():
	d = dict()
	for line in open("../app/src/main/res/raw/orthography_hz_compatibility.txt"):
		hz, val = line.rstrip()
		d[hz] = val
	return d

def getSTVariants(level=2):
	d = dict()
	for line in open("tables/%s/STCharacters.tsv" % SOURCE):
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
	_lang = None
	_city = None
	tones = None
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

	def getColor(self, f):
		if f == "hz": return "#9D261D"
		if "_" not in f: return "#1E90FF"
		if f.startswith("ltc_") or f.startswith("och_"): return "#4D4D4D"
		if f.startswith("cjy_"): return "#00008B"
		if f.startswith("cmn_jh_"): return "#800080"
		if f.startswith("cmn_jil_"): return "#A60918"
		if f.startswith("cmn_zho_"): return "#FF00C7"
		if f.startswith("cmn_ly_"): return "#C9007D"
		if f.startswith("cmn_xn_"): return "#C600FF"
		if f.startswith("cmn_"): return "#FF0000"
		if f.startswith("hsn_"): return "#FF69B4"
		if f.startswith("wuu_"): return "#0000FF"
		if f.startswith("nan_"): return "#FF6600"
		if f.startswith("mnp_"): return "#AC5370"
		if f.startswith("cdo_"): return "#DB7093"
		if f.startswith("gan_"): return "#20B2AA"
		if f.startswith("czh_"): return "#1E90FF"
		if f.startswith("hak_"): return "#008000"
		if f.startswith("wxa_"): return "#E7348D"
		if f.startswith("yue_"): return "#FFAD00"
		if f.startswith("csp_"): return "#FF9900"
		if f.startswith("xxx_"): return "#E600B1"
		return "#8B0000"

	@property
	def color(self):
		return ",".join(map(self.getColor, self.key.split(",")))

	@property
	def spath(self):
		sname = self._file
		if not sname: sname = f"{self.city}.tsv"
		if not sname.startswith("/"):
			sname = os.path.join(self.path, SOURCE, sname)
		g = glob(sname)
		if g:
			if len(g) == 1: return g[0]
		print(sname, g)
	
	def get_fullname(self, name):
		return os.path.join(self.path, SOURCE, name)

	@property
	def tpath(self):
		tpath = os.path.join(self.path, TARGET, self.city)
		if not tpath.endswith(".tsv"): tpath += ".tsv"
		return tpath

	@property
	def city(self):
		if self._city: return self._city
		return self.__module__.split(".")[-1]
	
	def __str__(self):
		return self.key.split(",")[0]

	@property
	def lang(self):
		if self._lang: return self._lang
		if self.city.endswith("話"): return self.city
		return f"{self.city}話"

	@property
	def desc(self):
		if self.count > 0: return "字數：%d<br><br>%s"%(self.count, self.note)
		return self.note
		
	@property
	def head(self):
		return str(self), self.lang, self.city, self.color, self.site, self.url, self.desc, self.tones

	def outdated(self):
		classfile = inspect.getfile(self.__class__)
		classtime = os.path.getmtime(classfile)
		ftime = os.path.getmtime(self.spath)
		if os.path.exists(self.tpath):
			if not os.path.exists(self.spath):
				return False
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

	def norm(self, yb):
		yb = yb.replace("᷉", "̃")\
			.replace("ʦ", "ts").replace("ʨ", "tɕ").replace("ʧ", "tʃ")\
			.replace("ʣ", "dz").replace("ʥ", "dʑ")
		return yb
	
	def isOldChinese(self):
		return self.key.startswith("ltc_") or self.key.startswith("och_")

	def isForeign(self):
		return self.key.startswith("vi_") or self.key.startswith("ko_") or self.key.startswith("ja_")

	def isDialect(self):
		return self.isLang() and not self.isForeign() and not self.isOldChinese()

	def write(self, d):
		self.patch(d)
		t = open(self.tpath, "w")
		print(f"#漢字\t音標\t解釋#{self.head}", file=t)
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
				py = py.strip().replace("Ǿ", "ˀ")
				if "\t" in py:
					yb, js = py.split("\t", 1)
					yb = yb.strip()
					js = js.strip().replace("~", "～")
				else:
					yb, js = py, ""
				if self.isLang() and self.isYb:
					yb = yb.lower().replace("g", "ɡ")
					if self.ybTrimSpace:
						yb = yb.replace(" ", "")
				yb = f"{yb}\t{js}"
				yb = self.norm(yb)
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
		for line in open(self.tpath):
			line = line.strip()
			if line.startswith("#"): continue
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
		
		for line in open(self.spath):
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
