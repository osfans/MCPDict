#!/usr/bin/env python3

from tables import *
import os
import logging
from time import time
from collections import defaultdict
from glob import glob
import inspect

SOURCE = "source"
TARGET = "target"

class 表:
	site = None
	url = None
	path = os.path.dirname(os.path.abspath(__file__))
	_time = os.path.getmtime(__file__)
	_file = None
	_color = None
	_lang = None
	_city = None

	@property
	def color(self):
		if self._color: return self._color
		f = self.key
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
		if f.startswith("gan_"): return "#20B2AA"
		if f.startswith("czh_"): return "#1E90FF"
		if f.startswith("hak_"): return "#008000"
		if f.startswith("wxa_"): return "#E7348D"
		if f.startswith("ltc_"): return "#4D4D4D"
		if f.startswith("och_"): return "#4D4D4D"
		return "#000000"

	@property
	def spath(self):
		sname = self._file
		if not sname: sname = "%s.tsv" % self.city
		spath = os.path.join(self.path, SOURCE, sname)
		g = glob(spath)
		if g:
			if len(g) == 1: return g[0]
			print(g)
	
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
		return self.key

	@property
	def lang(self):
		if self._lang: return self._lang
		if self.city.endswith("話"): return self.city
		return "%s話"%self.city

	@property
	def head(self):
		return self.key, self.lang, self.city, self.color, self.site, self.url, self.note, self.tones

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
	
	def write(self, d):
		t = open(self.tpath, "w")
		print("#", self.head, file=t)
		for hz in sorted(d.keys()):
			for py in d[hz]:
				py = py.strip().replace("~", "～")
				print("%s\t%s" % (hz, py),file=t)
		t.close()
	
	def read(self):
		start = time()
		if self.outdated(): self.update()
		d = defaultdict(list)
		for line in open(self.tpath):
			line = line.strip()
			if line.startswith("#"): continue
			hz, py = line.split("\t", 1)
			if "\t" in py: py = py.replace("\t", "{") + "}"
			d[hz].append(py)
		passed = time() - start
		logging.info(f"({len(d):5d}) {passed:6.3f} {self.city}")
		return d
	
	def parse(self, fs):
		return tuple(fs[:3])

	def format(self, line):
		return line

	def update(self):
		d = defaultdict(list)
		firstline = False
		for line in open(self.spath):
			if not firstline:
				firstline = True
				continue
			#if line.startswith('#'): continue
			line = self.format(line)
			fs = [i.strip('" ') for i in line.strip('\n').split('\t')]
			entries = self.parse(fs)
			if not entries: continue
			if type(entries) is tuple: entries = [entries]
			for fs in entries:
				if len(fs) <= 1: continue
				if len(fs) == 2:
					hz, yb = fs
					js = ""
				else:
					hz, yb, js = fs[:3]
				if not hz or len(hz.strip()) != 1: continue
				if not yb: continue
				p = "%s\t%s" % (yb, js)
				hz = hz.strip()
				p = p.strip()
				if p not in d[hz]:
					d[hz].append(p)
		self.write(d)
