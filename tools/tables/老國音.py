#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "ltc_lgy"
	_file = "lau_guoq_in.dict.yaml"

	def parse(self, fs):
		if len(fs) < 2: return
		hz, py = fs[:2]
		py = re.sub("(^|[^iy])eq$", "\\1ə5", py)
		py = re.sub("q$", "5", py)
		py = py.replace("ng", "ŋ")
		py = re.sub("^n([iy])", "ȵ\\1", py)
		py = py.replace("iu","iou").replace("ou", "əu").replace("ui", "uei").replace("er", "ɚ").replace("rw", "rʅ").replace("w", "ɿ")
		py = py.replace("p", "pʰ").replace("b", "p")
		py = py.replace("t", "tʰ").replace("d", "t")
		py = py.replace("j", "tɕ").replace("q", "tɕʰ").replace("x", "ɕ")
		py = py.replace("k", "kʰ").replace("g", "k").replace("h", "x")
		py = py.replace("zr", "tʂ").replace("cr", "tʂʰ").replace("sr", "ʂ").replace("r", "ɻ")
		py = py.replace("z", "ts").replace("c", "tsʰ")
		py = py.replace("yŋ", "iuŋ").replace("un","uen").replace("en","ən").replace("eŋ","əŋ")
		return hz, py
