#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	disorder = True

	def parse(self, fs):
		if len(fs) < 4: return
		name = str(self)
		if name in ("塔玆語", "海倫","宜興南",):
			hz, sy, tv, js = fs[:4]
		elif name in ("虎林", "吳江菀坪","景寧鄭坑","慈谿觀海衛","馬鞍山", "南陵", "南陵湖南街"):
			hz, sm, ym, tv, js = fs[:5]
			sy = sm + ym
		elif name in ("滁州",):
			_, hz, sm, ym, tv, js = fs[:6]
			sy = sm + ym
		elif name in ("宣平",):
			hz, _, sy, js = fs[:4]
			sy, tv = self.splitSySd(sy)
		else: #if name in ("淮南","懷遠","鳳陽","陽新新街",):
			_, hz, sm, ym, tv, _, js = fs[:7]
			sy = sm + ym
		if not hz or tv == "调": return
		yb = self.dz2dl(sy, tv)
		return hz, yb, js

