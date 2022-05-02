#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

#https://github.com/uliloewi/lang2jin1/blob/master/langjin.dict.yaml
ljsm = {'g': 'k', 'd': 't', '': '', 'sh': 'ʂ', 'c': 'tsʰ', 'b': 'p', 'l': 'l', 'h': 'x', 'r': 'ʐ', 'zh': 'ʈʂ', 't': 'tʰ', 'v': 'v', 'ng': 'ŋ', 'q': 'tɕʰ', 'z': 'ts', 'j': 'tɕ', 'f': 'f', 'ch': 'ʈʂʰ', 'k': 'kʰ', 'n': 'n', 'x': 'ɕ', 'm': 'm', 's': 's', 'p': 'pʰ'}
def py2yb(py):
	py = re.sub("r([1-5])$", "ʅ\\1", py)
	if py.startswith("ʅ"): py = "r" + py
	#fs = re.findall("^([^aäüeiouyʅ1-9]+)?(.*)(\d)?$", py)
	sm = py[:2]
	if sm not in ljsm: sm = py[0]
	if sm not in ljsm: sm = ""
	sd = py[-1]
	ym = py[len(sm):-1]
	if not sd.isdigit():
		ym = ym + sd
		sd = ""
	sm = ljsm[sm]
	ym = ym.replace("y", "ɿ").replace("ü", "y").replace("än", "ẽ").replace("ä", "ɛ")\
		.replace("ao", "ɔ").replace("ei", "əi").replace("ou", "əɯ")\
		.replace("en", "ə̃").replace("er", "ɚ").replace("ng", "̃").replace("n", "̃")
	return sm + ym + sd

class 表(_表):

	def parse(self, fs):
		if len(fs) < 2: return
		hz, py = fs[:2]
		if len(hz)!=1 or py in "vw": return
		yb = py2yb(py)
		return hz, yb
