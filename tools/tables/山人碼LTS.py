#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	_file = "ShanRenMaLTS.words.dict.yaml"
	short = "山人"
	note = "來源：https://github.com/siuze/ShanRenMaLTS"
	patches = {"□": "ak,akv"}

	def parse(self, fs):
		if len(fs) < 2: return
		return fs[0], fs[1]
