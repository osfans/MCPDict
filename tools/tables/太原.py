#!/usr/bin/env python3

from tables._音節表 import 字表 as 表

class 字表(表):
	key = "cjy_bz_ty"
	toneNames = {"平聲11": 1, "上聲53": 3, "去聲45": 5, "陰入2": 7, "陽入54": 8}
