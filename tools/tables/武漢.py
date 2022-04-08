#!/usr/bin/env python3

from tables._音節表 import 字表 as 表

class 字表(表):
	key = "cmn_xn_hg_wh"
	toneNames = {"陰平55": 1, "陽平213": 2, "上聲42": 3, "去聲35": 5}
