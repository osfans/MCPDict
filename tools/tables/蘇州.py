#!/usr/bin/env python3

import re
from tables._音典 import 表 as _表

class 表(_表):
	_files = ["苏州（记音替换版）1.1.tsv", "苏州.tsv"]
	loosedict = dict()
	ybdict = dict()

	def parse(self, fs):
		if self.current_file == self._files[0]:
			order, hz, sm, ym, sd, js = fs[:6]
			self.loosedict[order+hz] = sm+ym+sd
			return
		order, hz, sm, ym, sd, js = fs[:6]
		yb = sm + ym + sd
		loose = self.loosedict.get(order+hz, self.ybdict.get(yb, ""))
		if loose and loose != yb.replace("*", ""):
			self.ybdict[yb] = loose
			yb = yb + "/" + loose
		return hz, yb, js

