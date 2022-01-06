#!/usr/bin/env python3

from tables._表 import 表
import re

class 字表(表):
	key = "xxx_nshu"
	_lang = "江永上江圩土話"
	_file = "unicode_nushu_data.csv"
	note = "更新：2021-08-27<br>來源：<a href=https://nushuscript.org/>在線女書字典</a><br>說明：女書，又名江永女書，是一種獨特的漢語書寫系統。它是一種專門由女性使用的文字，起源於中國湖南省南部永州的江永縣。其一般被用來書寫江永城關方言。以前在江永縣及其毗鄰的道縣、江華瑤族自治縣的大瑤山、以及廣西部分地區的婦女之間流行、傳承。<br>注明女書寫法的來自趙麗明、徐焰編纂的《女書規範字書法字帖》，注明女書編號的來自宮哲兵、唐功暐編纂的《女書標準字字典》，兩者音系不完全相同。"
	tones = "44 1 1a 陰平 ꜀,42 2 1b 陽平 ꜁,35 3 2a 陰上 ꜂,13 4 2b 陽上 ꜃,21 5 3a 陰去 ꜄,33 6 3b 陽去 ꜅,5 7 4 入 ꜆"
	toneValues = ['44','42','35','13','21','33','5']
	simplified = 2

	def parse(self, fs):
		ns = fs[0]
		if len(ns) != 1: return
		hzs = fs[2]
		py = fs[3]
		py = re.sub("^h", "x", py)
		py = py.replace("nj", "ȵ").replace('ng', 'ŋ').replace("c", "ɕ").replace('h', 'ʰ')
		py = py.replace("oe", "ø").replace('e', 'ə').replace('iə', 'ie').replace('w', 'ɯ')
		tone = re.findall('\d+', py)[0]
		tonetype = str(self.toneValues.index(tone)+1)
		py = py.replace(tone, tonetype)
		l = list()
		for hz in hzs:
			yb = py + ns
			l.append((hz, yb))
		return l
	
	def py2ipa(self, py):
		py = py.replace("yueng", "yun").replace("yiong", "ing")
		py = re.sub("^y([^iu])", "i\\1", py)
		py = py.replace('yu', 'y').replace('yiu', 'yu').replace('yi', 'i')
		py = re.sub('([jqx])u', '\\1y', py)
		py = re.sub('([jqx])iu', '\\1yu', py)
		py = re.sub('([jqx])ou', '\\1iou', py)
		py = py.replace("nj", "ȵ").replace('ng', 'ŋ')
		py = py.replace("p", "pʰ").replace('b', 'p')
		py = py.replace("t", "tʰ").replace('d', 't')
		py = py.replace("k", "kʰ").replace('g', 'k')
		py = py.replace("c", "tsʰ").replace('z', 'ts')
		py = py.replace("q", "tɕʰ").replace('j', 'tɕ').replace('x', 'ɕ').replace('h', 'x').replace('w', 'v')
		py = py.replace('ao', 'au').replace('e', 'ə').replace('iə', 'ie')
		py = re.sub('o(\d)', 'ø\\1', py)
		return py

	def patch(self, d):
		tones = ['33','42','35','13','21','xx','5']
		for line in open(self.get_fullname("nsbzzzd.csv")):
			line = line.strip()
			fs = line.split(",")
			ns = fs[0]
			if not ns.isdigit(): continue
			hzs = fs[2]
			py = fs[1]
			tone = re.findall('\d+', py)[0]
			tonetype = str(tones.index(tone)+1)
			py = py.replace(tone, tonetype)
			yb = self.py2ipa(py)
			yb += "\t" + ns
			for hz in hzs:
				d[hz].append(yb)
