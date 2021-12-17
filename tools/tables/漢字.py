#!/usr/bin/env python3

from tables._表 import 表

class 字表(表):
	key = "hz"
	_lang = "漢字"
	_color = "#9D261D"
	site = "漢字音典在線版"
	url = "https://mcpdict.sourceforge.io/cgi-bin/search.py?hz=%s"
	note = "　　本程序源自“<a href=https://github.com/MaigoAkisame/MCPDict>漢字古今中外讀音查詢</a>”，收錄了更多語言、更多讀音，錯誤也更多，可去<a href=https://github.com/osfans/MCPDict>GitHub</a>、<a href=mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D-hNzAQCgZQL-uIlhFrxWJ56umCexsmBi>QQ群</a>、<a href=https://www.coolapk.com/apk/com.osfans.mcpdict>酷安</a>提出意見与建議、提供同音字表請求收錄。<br>　　本程序將多種語言的漢字讀音集成於本地數據庫，默認用國際音標注音，可用於比較各語言讀音的異同，也能給學習本程序所收的語言提供有限的幫助。方言分片以《中國語言地圖集》（第二版）为主。<br>　　本程序收錄了統一碼14.0全部漢字（包含“鿽鿾鿿𪛞𪛟𫜵𫜶𫜷𫜸”，不包含部首及兼容區漢字）、〇（同“星”或“零”）、□（有音無字、本字不明）。支持形音義等多種查詢方式，可輸入𰻞（漢字）、30EDE（統一碼）、biang2（普通話拼音，音節末尾的“?”可匹配任何聲調）、43（總筆畫數）、辵39（部首餘筆），均可查到“𰻞”字，也可以選擇兩分、五筆畫等輸入形碼進行查詢，還可以選擇說文解字、康熙字典、漢語大字典等通過釋義中出現的詞語搜索到相應的漢字。"

	def read(self):
		return dict()
