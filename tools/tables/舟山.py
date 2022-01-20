#!/usr/bin/env python3

from tables._甬江 import 字表 as 表

class 字表(表):
	key = "wuu_th_yj_zs"
	_file = "定海.tsv"
	note = """來源：<a href=https://github.com/ionkaon/dictionary>甬江話字詞表</a><br>說明：舟山城區字表整理自方松熹：《舟山方言研究》，社會科學文獻出版社，1993"""
	tones = "53 1 1a 陰平 ꜀,22 2 1b 陽平 ꜁,35 3 2a 陰上 ꜂,24 4 2b 陽上 ꜃,44 5 3a 陰去 ꜄,13 6 3b 陽去 ꜅,5 7 4a 陰入 ꜆,12 8 4b 陽入 ꜇"
	toneValues = {"53":1, "22":2, "35":3, "24":4, "44":5, "13":6, "5":7, "12":8}
