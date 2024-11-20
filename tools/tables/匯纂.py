#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	full = "古音匯纂"
	_file = "古音匯纂.tsv"
	note = ""
	dictionary = True
	
	ybTrimSpace = False
	
	def parse(self, fs):
		return fs[0], fs[1], "\t".join(fs[2:]).replace("▲", "\t▲")
