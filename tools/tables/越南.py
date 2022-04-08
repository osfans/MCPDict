#!/usr/bin/env python3

from tables._數據庫 import 字表 as 表

class 字表(表):
	key = "vi_"
	dbkey = "vn"
	site = "漢越辭典摘引"
	url = "http://www.vanlangsj.org/hanviet/hv_timchu.php?unichar=%s"
