#!/usr/bin/env python3

from tables._音節表 import 字表 as 表

class 字表(表):
	key = "hsn_xt"
	toneNames = {"陰平33": 1, "陽平12": 2, "上聲42": 3, "陰去55": 5, "陽去21": 6, "入聲24": 7}
