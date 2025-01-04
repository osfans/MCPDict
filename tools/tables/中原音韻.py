#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	網站 = '韻典網（中原音韻）'
	網址 = 'https://ytenx.org/trngyan/dzih/%s'
	
	def __init__(自):
		自.sds = {
			'陰': '1',
			'陽': '2', '入作陽': '2', '去作陽': '2',
			'上': '3',
			'去': '4', '入作去': '4', '入作上': '3',
		}

	def 析(自, 列):
		小韻, 字組, 聲母, 韻母, sd, 楊耐思, 寧繼福, 薛鳳生_音位, unt_音位, unt, 釋義, 校註 = 列
		音組 = [楊耐思, 寧繼福, 薛鳳生_音位, unt_音位, unt]
		for i, 音 in enumerate(音組):
			# 音標改爲今天習慣
			音 = 音.replace('ɽ', 'ɻ')
			音 = 音.replace('ʻ', 'ʰ')
			音 = re.sub('([ʂɻʃʒ].*?)ï', '\\1ʅ', 音).replace('ï', 'ɿ')
			if i == 2:  # 薛鳳生_音位
				音 = re.sub('^h', 'x', 音)
				音 = 音.replace('h', 'ʰ')
				音 = 音.replace('c', 'ts')
				音 = 音.replace('sr', 'ʂ').replace('r', 'ɻ')  # 噝音後的 r 實爲捲舌標記
				音 = 音.replace('y', 'j')
			音 += 自.sds[sd]
			音組[i] = 音
		音組 = '/'.join(音組)
		if sd.startswith('入') or sd == '去作陽':
			音組 = f'**{音組}**'

		if 校註:
			校註 = '校註：' + 校註
		js = [釋義, 校註]
		js = [i for i in js if i]
		js = '；'.join(js)
		l = list()
		for 字 in 字組:
			l.append((字, 音組, js))
		return l
