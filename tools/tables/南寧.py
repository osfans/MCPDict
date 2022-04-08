#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "yue_nnbh"
	_file = "南寧白話單字音表(總表).tsv"
	note = "來源：<a href=https://github.com/leimaau/naamning_jyutping>南寧話輸入方案</a><br>說明：心母字讀 sl[ɬ]（清齒齦邊擦音），效咸山攝二等字讀 -eu[-ɛu]、-em[-ɛm]/-ep[-ɛp]、-en[-ɛn]/-et[-ɛt]<br>老派的師韻（止開三精莊組）字讀 zy[tsɿ]、cy[tsʰɿ]、sy[sɿ]，津韻（臻合三舌齒音、部份臻開三）字讀 -yun[-yn]/-yut[-yt]<br>(白)白讀；(文)文讀；(老派)老派音；(習)習讀；(常)常讀；(又)又讀；(罕)罕讀；(訓)訓讀；(舊)舊讀；(語)口語音；(書)書面音；(外)外來語音譯；(名)名詞；(動)動詞；(量)量詞"
  
	def parse(self, fs):
		_,hz,_,yb,py,js,c = fs
		sd = py[-1]
		enter = py[-2]
		if enter in "ptk":
			if sd == "1": sd = "7"
			elif sd == "3": sd = "8"
			else: sd = "9"
		yb = yb.rstrip("012345") + sd
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
		elif c == "※":
			pass
		else:
			js = c + js
		return hz, yb, js
