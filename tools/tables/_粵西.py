#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	_file = "粤西闽语方言字表*.tsv"

	def parse(self, fs):
		if len(fs) < 6: return
		hz = fs[0]
		if hz == "例字": return
		ybs = fs[self.index]
		ybs = ybs.replace("／", "/")
		if not ybs or ybs.startswith("—"): return
		_js = hz[1:] if len(hz)>1 else ""
		_js = _js.strip("（）")
		hz = hz[0]
		l = list()
		for _yb in ybs.split("/"):
			_yb = _yb.strip()
			c = ""
			if "（" in _yb:
				n = _yb.index("（")
				c = _yb[n:]
				_yb = _yb[:n]
				if c == "（白）":
					c = ""
					_yb += "-"
				elif c == "（文）":
					c = ""
					_yb += "="
			sd = re.findall("\d+", _yb)[0]
			yb = _yb.replace(sd, str(self.toneValues[sd]))
			js = c + _js
			if js.startswith("（") and js.endswith("）"):
				js = js[1:-1]
			l.append((hz, yb, js))
		return l

