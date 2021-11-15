#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "ltc_lgy"
	note = "來源：<a href=https://github.com/jacob-us/lau_guoq_in/>老國音輸入灋方案</a>、<a href=https://zhuanlan.zhihu.com/p/21674298>老國音輸入方案簡介</a><br>說明：老國音，前民國教育部討論頒行的全國統一語音規範。 與現行普通話/國語相比，增加微疑兩母以及入聲，聲韻配合與些許字音也有不同。 本方案爲基於《校改國音字典》的老國音輸入灋，元書中發現有紕漏乃至錯誤之處一併糾正。"
	tones = "33 1 1a 陰平 ꜀,35 2 1b 陽平 ꜁,214 3 2 上 ꜂,51 5 3 去 ꜄,5 7 4 入 ꜆"
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
