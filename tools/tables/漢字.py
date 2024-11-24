#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	color = "#9D261D"
	site = "漢字音典在線版"
	url = "https://mcpdict.sourceforge.io/cgi-bin/search.py?hz=%s"
	note = """　　本程序源自“<a href=https://github.com/MaigoAkisame/MCPDict>漢字古今中外讀音查詢</a>”，收錄了更多語言、更多讀音，錯誤也更多，可去<a href=mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D-hNzAQCgZQL-uIlhFrxWJ56umCexsmBi>QQ群</a>、<a href=https://github.com/osfans/MCPDict>GitHub</a>提出意見与建議、提供同音字表請求收錄。<br>
　　本程序將數百種語言（方言）的漢字讀音集成於本地數據庫，使用國際音標注音，可比較各語言的異同，也能給語言學習者提供有限的幫助。有多種方言分區方案供選擇。<br>
　　本程序收錄了統一碼16.0全部漢字（不包含部首及兼容區漢字）、〇（同“星”或“零”）、□（有音無字、本字不明），可使用文津宋體顯示所有漢字。支持形音義等多種查詢方式，可輸入𰻞（漢字）、30EDE（統一碼）、biang2（普通話拼音，音節末尾的“?”可匹配任何聲調）、43（總筆畫數）、辵39（部首餘筆），查到“𰻞”字。也可選擇兩分、五筆畫等形碼進行查詢，亦可選擇說文解字、康熙字典、漢語大字典、古音匯纂等通過釋義中出現的詞語搜索到相應的漢字。"""

	def read(self):
		return dict()
