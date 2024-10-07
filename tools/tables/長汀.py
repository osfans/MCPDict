#!/usr/bin/env python3

from tables._縣志 import 表 as _表
import re

class 表(_表):

	def format(self,line):
		if line.startswith("#"): return line
		line = line.replace("（","{").replace("）","}")
		line = line.replace("[","［").replace("]","］")
		line = line.replace("*（", "□（")
		line = re.sub("\*(.)", "\\1?", line)
		line = re.sub("［(.)(.*?)］", "\\1*\\2", line)
		fs = line.split("\t")
		for i,sd in enumerate(self.toneMaps.values()):
			fs[i + 1] = f"[{sd}]" + fs[i + 1]
		line = "".join(fs)
		return line
