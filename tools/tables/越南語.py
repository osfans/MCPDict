#!/usr/bin/env python3

from tables._數據庫 import 表 as _表

class 表(_表):
	dbkey = "vn"
	site = "漢越辭典摘引"
	url = "http://www.vanlangsj.org/hanviet/hv_timchu.php?unichar=%s"

	@property
	def syCount(self):
		return len(set(map(lambda x:x.rstrip("zrsfxj"), self.syds.keys())))
