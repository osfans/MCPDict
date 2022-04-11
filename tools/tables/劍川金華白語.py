#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):

	def parse(self, fs):
		hz, sy, sd, js = fs[:4]
		yb = sy + sd
		return hz, yb, js
