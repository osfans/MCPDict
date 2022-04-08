#!/usr/bin/env python3

from tables._縣志 import 字表 as 表
import re

class 字表(表):
	key = "cjy_bz_jx"
	_file = "介休话*.tsv"
