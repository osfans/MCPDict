#!/usr/bin/env python3

from tables._跳跳老鼠 import 表 as _表
import re

class 表(_表):
	disorder = True

	def parse(self, fs):
		yb, sd, _, hzs = fs[:4]
		return _表.parse(表, (yb, sd, hzs))

