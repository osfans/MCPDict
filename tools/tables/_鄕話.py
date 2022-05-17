#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	_file = "鄉話*.tsv"
	names = ("沅陵深溪口","沅陵麻溪鋪","沅陵淸水坪","沅陵棋坪","古丈高峯","瀘溪八什坪","瀘溪","沅陵丑溪口","沅陵渭溪","漵浦木溪")

	def parse(self, fs):
		name = str(self)
		index = self.names.index(name) * 2 + 2
		hz, js = fs[:2]
		yb = fs[index] + fs[index+1]
		return hz, yb, js

