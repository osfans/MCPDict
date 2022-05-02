#!/usr/bin/env python3

from tables._吳語 import 表 as _表

class 表(_表):

	def parse(self, fs):
		#hz,_,yb,py,js = fs[:5]
		_,yb,_,hz,js,_,py = fs[:7]
		return _表.parse(self, [hz,_,yb,py,js])
