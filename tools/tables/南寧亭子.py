#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "csp_nntz"
	_file = "南寧亭子平話單字音表(總表).tsv"
	_lang = "南寧亭子平話"
	note = "來源：<a href=https://github.com/leimaau/naamning_jyutping>南寧話輸入方案</a><br>說明：<br>心母字讀 sl[ɬ]（清齒齦邊擦音），日母和疑母細音字讀 nj[ȵ]（齦齶音）<br>老派的疑母模韻字讀 ngu[ŋu]，微母遇攝臻攝字讀 fu[fu]、fat[fɐt]、fan[fɐn]，遇合一讀o[o]，果合一讀u[u]<br> (白)白讀；(文)文讀；(老派)老派音；(習)習讀；(常)常讀；(又)又讀；(罕)罕讀；(訓)訓讀；(舊)舊讀；(語)口語音；(書)書面音；(外)外來語音譯；(名)名詞；(動)動詞；(量)量詞"
	tones = "53 1 1a 陰平 ꜀,33 3 2a 陰上 ꜂,55 5 3a 陰去 ꜄,21 2 1b 陽平 ꜁,24 4 2b 陽上 ꜃,22 6 3b 陽去 ꜅,5 7a 4a 上陰入 ꜆,3 7b 4b 下陰入 ꜀,24 8a 4c 上陽入 ꜇,2 8b 4d 下陽入 ꜁"

	def parse(self, fs):
		_,hz,_,yb,py,js,c = fs
		sd = py[-1]
		enter = py[-2]
		if enter in "ptk":
			if sd == "3": sd = "7"
			elif sd == "2": sd = "8"
			elif sd == "5": sd = "9"
			else: sd = "10"
		yb = yb.lstrip("∅").rstrip("012345") + sd
		if c == "(白)":
			yb += "-"
		elif c == "(文)":
			yb += "="
		elif c == "(又)":
			yb += "+"
		elif c == "(訓)":
			yb += "~"
		elif c == "(語)":
			yb += "\\"
		elif c == "(書)":
			yb += "/"
		elif c == "(習)" or c == "(常)":
			yb += "*"
		elif c == "※":
			pass
		else:
			js = c + js
		return hz, yb, js

