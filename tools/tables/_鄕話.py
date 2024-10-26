#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	_file = "鄉話*.xlsx"
	indexs = {'沅陵深溪口': 2, '沅陵麻溪鋪': 5, '沅陵淸水坪': 12, '沅陵棋坪': 15, '古丈髙峯': 18, '瀘溪八什坪': 29, '瀘溪': 32, '沅陵丑溪口': 35, '沅陵渭溪': 38, '漵浦木溪': 41}


	def parse(self, fs):
		name = str(self)
		index = self.indexs[name]
		hz, js = fs[:2]
		yb = "".join(fs[index:index+3])
		return hz, yb, js

