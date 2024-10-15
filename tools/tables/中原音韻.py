#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	#https://github.com/BYVoid/ytenx/blob/master/ytenx/sync/trngyan
	site = '韻典網（中原音韻）'
	url = 'https://ytenx.org/trngyan/dzih/%s'
	
	def __init__(self):
		self.sds = {
			'陰': '1',
			'陽': '2', '入作陽': '2', '去作陽': '2',
			'上': '3',
			'去': '4', '入作去': '4', '入作上': '3',
		}

	def parse(self, fs):
		小韻, hzs, 聲母, 韻母, sd, 楊耐思, 寧繼福, 薛鳳生_音位, unt_音位, unt, 釋義, 校註 = fs
		ybs = [楊耐思, 寧繼福, 薛鳳生_音位, unt_音位, unt]
		for i, yb in enumerate(ybs):
			# 音標改爲今天習慣
			yb = yb.replace('ɽ', 'ɻ')
			yb = yb.replace('ʻ', 'ʰ')
			yb = re.sub('([ʂɻʃʒ].*?)ï', '\\1ʅ', yb).replace('ï', 'ɿ')
			if i == 2:  # 薛鳳生_音位
				yb = re.sub('^h', 'x', yb)
				yb = yb.replace('h', 'ʰ')
				yb = yb.replace('c', 'ts')
				yb = yb.replace('sr', 'ʂ').replace('r', 'ɻ')  # 噝音後的 r 實爲捲舌標記
				yb = yb.replace('y', 'j')
			yb += self.sds[sd]
			if sd.startswith('入') or sd == '去作陽':
				yb = f'*{yb}*'
			ybs[i] = yb
		ybs = '/'.join(ybs).replace("*/*", "/")

		if 校註:
			校註 = '校註：' + 校註
		js = [釋義, 校註]
		js = [i for i in js if i]
		js = '；'.join(js)
		l = list()
		for hz in hzs:
			l.append((hz, ybs, js))
		return l
