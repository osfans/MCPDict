#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "wuu_th_shj_sz_ltc"
	note = "版本：1.2(2121-12-29)<br>來源：樛木整理自《一百年前的蘇州話》、《蘇州同音字彙1892》、《鄉音字類》<br>說明：加*的漢字表示[ʐ][dʐ]歸屬、陽上陽去歸屬缺乏直接資料，以區分這兩者的蘇州行政區劃內的方吳語爲基準。"
	tones = "44 1 1a 陰平 ꜀,223 2 1b 陽平 ꜁,51 3 2a 陰上 ꜂,31 4 2b 陽上 ꜃,523 5 3a 陰去 ꜄,312 6 3b 陽去 ꜅,4 7 4a 陰入 ꜆,23 8 4b 陽入 ꜇"
	_color = "#0000FF,#4D4D4D"
	_file = "清末蘇州*.tsv"

	def parse(self, fs):
		hz, jt, py, js = fs[:4]
		py = py.replace("øʏ","𐞢ʏ")
		if py.endswith("="):
			js = "(書)%s" % js
		elif py.endswith("-"):
			py = py[:-1] + "="
		js = js.strip()
		if not hz: hz = jt
		return hz, py, js

