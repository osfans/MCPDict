#!/usr/bin/env python3

from tables._數據庫 import 字表 as 表

class 字表(表):
	key = "vi_"
	dbkey = "vn"
	_lang = "越南語"
	_color = "#8B0000"
	site = "漢越辭典摘引"
	url = "http://www.vanlangsj.org/hanviet/hv_timchu.php?unichar=%s"
	note = "來源：<a href=http://www.vanlangsj.org/hanviet/>漢越辭典摘引</a>"
	tones = "33 1 1a 陰平 ꜀,21 2 1b 陽平 ꜁,313 3 2a 陰上 ꜂,35 4 2b 陽上 ꜃,35 5 3a 陰去 ꜄,21 6 3b 陽去 ꜅,35 7 4a 陰入 ꜆,21 8 4b 陽入 ꜇"
