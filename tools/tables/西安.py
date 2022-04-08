#!/usr/bin/env python3

from tables._音節表 import 字表 as 表

class 字表(表):
	key = "cmn_zho_xa"
	toneNames = {"陰平21": 1, "陽平24": 2, "上聲53": 3, "去聲44": 5}
