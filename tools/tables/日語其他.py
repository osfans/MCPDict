#!/usr/bin/env python3

import sqlite3, re
from collections import defaultdict
from tables._數據庫 import 表 as _表

class 表(_表):
	note = "來源：《漢字源》改訂第五版<br>說明：《漢字源》區分了漢字的吳音、漢音、唐音與慣用音，並提供了“歷史假名遣”寫法。該辭典曾經有<a href=http://ocn.study.goo.ne.jp/online/contents/kanjigen/>在線版本</a>，但已於2014年1月底終止服務。<br>　　日語每個漢字一般具有吳音、漢音兩種讀音，個別漢字還具有唐音和慣用音。這四種讀音分別用“日吳”、“日漢”、“日唐”、“日慣”表示。另外，對於一些生僻字，《漢字源》中沒有註明讀音的種類，也沒有提供“歷史假名遣”寫法，這一類“其他”讀音用“日他”表示。<br>　　 有的讀音會帶有括號，括號前的讀音爲“現代假名遣”寫法，括號內的讀音爲對應的“歷史假名遣”寫法。<br>　　 讀音的顏色和粗細代表讀音的常用程度。<b>黑色粗體</b>爲“常用漢字表”內的讀音；黑色細體爲《漢字源》中列第一位，但不在“常用漢字表”中的讀音；<span class=dim>灰色細體</span>爲既不在“常用漢字表”中，也不在《漢字源》中列第一位的讀音。"
	
	def update(self):
		d = defaultdict(list)
		conn = sqlite3.connect(self.spath)
		conn.row_factory = sqlite3.Row
		c = conn.cursor()
		for r in c.execute('SELECT * FROM mcpdict'):
			hz = chr(int(r["unicode"],16))
			for dbkey in ["jp_tou", "jp_kwan", "jp_other"]:
				pys = r[dbkey]
				if not pys: continue
				pys = re.sub("\[\d\]", ",",pys).strip(",")
				for py in pys.split(","):
					py = py.strip()
					if not py: continue
					yb = self.format(py)
					if dbkey == "jp_tou":
						yb += "\t唐"
					elif dbkey == "jp_kwan":
						yb += "\t慣"
					d[hz].append(yb)
		conn.close()
		self.write(d)
