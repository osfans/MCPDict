#!/usr/bin/env python3

from tables._音節表 import 字表 as 表

class 字表(表):
	key = "cmn_jil_jn"
	toneNames = {"陰平213": 1, "陽平42": 2, "上聲55": 3, "去聲21": 5, "輕聲": ''}
