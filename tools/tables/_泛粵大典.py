#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	
	def parse(self, fs):
		if len(fs) < 4: return
		js = ""
		if str(self) == "開平沙塘":
			hz = fs[0]
			py = fs[2]+fs[3]
			yb = fs[5] + fs[6]
			js = fs[4]
		elif str(self) == "新會會城":
			hz, _, py, yb = fs[:4]
		else:
			hz = fs[0]
			py = fs[3]+fs[4]
			yb = fs[14] + fs[15]
			js = fs[17]
		if not py or not yb: return
		sd = py[-1]
		if not sd.isdigit(): sd = "0"
		py = py.rstrip("1234567890")
		if py and py[-1] in "ptk":
			if sd == "1": sd = "8"
			elif sd == "2": sd = "7"
			elif sd == "4": sd = "10"
			elif sd == "6": sd = "9"
		yb = yb.rstrip("1234567890") + sd
		return hz, yb, js
