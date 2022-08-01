#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):
	note = "來源：<a href=https://ytenx.org/kyonh/>韻典網</a>、<a href=https://nk2028.shn.hk/qieyun-js/>nk2028</a><br>說明：括號中註明了切韻音系中的聲母、呼、等、重紐類、韻、聲調，在晚期中古漢語中對應的攝，《廣韻》韻目以及對應的《平水韻》韻目。對於有重紐的韻，重紐類僅在聲母爲云以母以外的脣牙喉音時標註。呼在聲母爲脣音或韻母開合中立時不標註。"
	site = '韻典網（廣韻）'
	url = 'http://ytenx.org/zim?kyonh=1&dzih=%s'
	last = ""
	isYb = False

	def __init__(self):
		self.廣韻新 = dict()
		for line in open(self.get_fullname("廣韻新.tsv"), encoding="U8"):
			fs = line.strip("\n").split("\t")
			# fs = fs.replace("'", "0")  # TODO: 是否需要？
			小韻號 = fs[0]
			if fs[1] == '2':
				小韻號 += 'b'
			self.廣韻新[小韻號] = fs[2:]

	def parse(self, fs):
		hz = fs[0]
		js = fs[3]
		if js == "[同上]": js = "同上"
		if "上同" in js: js = js.replace("上同", "同" + self.last)
		elif "同上" in js: js = js.replace("同上", "同" + self.last)
		else: self.last = hz
		
		小韻號 = fs[1]
		需拆分小韻 = {
			'1043': '烋𠇾休',
			'1423': '柺枴杖拐',
			'3521': '訐䅥揭讦',
			'3708': '抑𡊁𢬃𡊶㧕枊𢮮𢂗𠨔𢑏',
		}
		if 小韻號 in 需拆分小韻 and hz in 需拆分小韻[小韻號]:
			小韻號 += 'b'
		小韻信息 = self.廣韻新[小韻號]
		反切 = 小韻信息[1]  # TODO: 暫不需要
		is平賅入 = True  # TODO: 需由上層傳入
		音韻地位 = ''.join(小韻信息[2:6]) + 小韻信息[6 if is平賅入 else 7] + 小韻信息[8]
		攝 = 小韻信息[9] + '攝'
		廣韻韻目 = '廣韻' + 小韻信息[10].split('聲')[1]
		平水韻目 = '平水' + 小韻信息[11].split('聲')[1]
		附註 = ' '.join([音韻地位, 攝, 廣韻韻目, 平水韻目] if 音韻地位 else ['訛字', 廣韻韻目, 平水韻目])

		拼音名 = ['切韻拼音', '古韻羅馬字', '有女羅馬字', '白一平轉寫']
		擬音名 = ['unt切韻擬音L', 'unt切韻擬音J', 'msoeg中古擬音V8']  # 其他擬音待補充
		拼音 = 小韻信息[12:16]
		擬音 = 小韻信息[16:]

		# TODO: 上層需要選擇使用哪個拼音哪個擬音
		# return hz, 拼音[0], 擬音[0], 附註, js
		return hz, 拼音[0], js
