#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables.__init__ import isHZ, isCompatible
from collections import defaultdict
import re, os

puas = """#	{1}	bottom of 廌焉舃 (F2A5 󰓰)	⿹⿺㇉一灬	
#	{2}	top of 免象 (F0CF 󶃛)	⿱⺈𫩏	
#	{3}	top of 夐 (F2A7 󰒭)	⿱⺈⿵冂人	
#	{4}	top of 䝉𫎇 (F313 =󰌨冖)	⿱⿻⿰丨丨丷冖
#	{5}	left of 叚 (F2A9 󰒨)	？	
#	{6}	left of 段 (F2AA 󰓃)	？	
#	{7}	top of 專惠 (F4F3 󰔲)	？	
#	{8}	right of 扥 (F2AB 󰑪)	⿻⿱㇒一乚	
#	{9}	right of 𪟳𪝪 (F2AC 󶇖)	⿱⿰㇒㇖丨
#	{10}	right of 巩 [T source glyph] (F2A8 󰐣)	⿻⿻㇈丿丶
#	{11}	bottom of 軣 (F2AD 󰐻)	⿰⿱丶㇀⿱㇒丶
#	{12}	right of 龍 (F2AE 󰐼)	？
#	{13}	outside of 虍 (F29F 󺪊)	⿸⿰丿⿱⺊⺂七
#	{14}	top of 𡕡 (F2B0 󰒈)	？
#	{15}	bottom-right of 旅 (F2B1 󰒠)	⿻𱍸㇒
#	{16}	bottom of 鼠 (F31A 󰓑)	⿲⿺𠄌⺀⿺𠄌⺀㇂
#	{17}	left of 剆 [G source glyph] (F2B3 󰒕)	？
#	{18}	left of 即 (F2B4 󰑴)	？
#	{19}	right of 㤤 (F2B5 󰕊)	？
#	{20}	right of 執 [G source glyph] (F5EE 󶏓)	丸
#	{21}	top of 祭豋 (F101 󰒛)	？
#	{22}	outside of 与 (F2E6 󰒁)	⿺㇉一
#	{23}	inside of 與 (F2E9 =与)	？
#	{24}	right of 水 (F2F2 󰌏)	？
#	{25}	bottom (legs) of 喪 (F2F4 󰒸)	？
#	{26}	top of 韲 (F2BA 󰓿)	？
#	{27}	bottom of 𤣥 (F2BB 󰐫)	？
#	{28}	outside of 武貮 (F2BC 󰒰)	⿻弋一
#	{29}	outside of 幾畿 (F2BD 󰑷)	⿻弋𢆶
#	{30}	top of 彔 (F2F5 󰒽)	？
#	{31}	bottom of 卥 (F2BF 󰉫)	⿴囗⿻𰀪丶
#	{32}	top of 𣓝 (F5FD 󰉬)	⿱⿻𠮛⿰丨丨冖
#	{33}	bottom of 雋 (F2C1 󰓹)	？
#	{34}	bottom of 䇖 (F2B2 󰓳)	⿹勹丿
#	{35}	right of 𠖵 (F2C3 =𲋄)	⿻几𠄠
#	{36}	top of 𠢘㥑 (F2C4 󰒾)	
#	{37}	top of 睿 (F2B6 󰋞)	⿱⺊冖
#	{38}	top of 橐 (F2B9 󰋐)	⿱𰀉冖
#	{39}	left of 卬 (F2C7 󰒲)	？
#	{40}	right of 𩤷 (F2C8 󰌯)	？
#	{41}	left of 𠜂 [T-form of 𠕋] (F2C6 =𠕋)	⿻𦉫𠄠
#	{42}	top of 录 (F2D4 =彐)	彐
#	{43}	top of 㚑 (F2A4 =⺕)	⺕
#	{44}	top of 孛 (F2C9 󰓔)	⿱十冖
#	{45}	left of 北 [G source glyph] (F5FB 󰒅)	？
#	{46}	bottom of 竜 (EC30 󰒏)	⿻日乚
#	{47}	outside of 鳥島 (F2CA 󰒱)	？
#	{48}	bottom of 年 [J-form of 㐄󰑵] (F5FC )	㐄
#	{49}	left of 帅 (F2D2 󰐱)	⿰丨丿
#	{50}	top right of 叚 (F319 󰒦)	？
#	{51}	outside of 馬 (F2CB 󰐲)	？
#	{52}	outside of 马 (F2E8 =𢎘)	？
#	{53}	outside of 鸟岛 (F2E7 󰐯)	？
#	{54}	outside of 烏 (F2FA 󰑍)	？
#	{55}	outside of 鹿慶 (F2D0 󰓗)	？
#	{56}	top of 無 (F2CF 󰒀)	？
#	{57}	outside of 在 (F2CE 󰐨)	？
#	{58}	top left of 留 (F2CD 󰓒)	⿰{39}丶
#	{59}	bottom of 舄 (F13C 󰒔)	？
#	{60}	outside of 龜 [H source glyph] (F5F6 󰓧)	？
#	{61}	outside of 臧 (F5F0 󶏔)	？
#	{62}	bottom of 華 (F13D 󰒓)	？
#	{63}	
#	{64}	bottom left of 龜 [G source glyph] (F314 =𢑑)	？
#	{65}	bottom right of 龜 [G source glyph] (F318 =龱)	？
#	{66}	bottom right of 𪚴 (F31B =𱕻)	？
#	{67}	middle of 髙 and 𠅘 (F2F7 󰑈)	⿴⿰丨丨𠄠
#	{68}	top of 黑 (F5F4 󰐙)	？
#	{69}	outside of 戠 (F5F5 󰐂)	？
#	{70}	inside of 虛 (F2D1 󰓠)	？
#	{71}	left of 𦥑 (F5FE 󰒬)	？
#	{72}	left of 印 [G source glyph] (F5FF 󰒬)	？
#	{73}	top of 具 (ECE0 󰑸)	⿴𠀃三
#	{74}	middle of 鄕 [J source glyph] (ECE1 =皀)	？
#	{75}	bottom left of 爵 [K source glyph] (ECE2 =󰑴)	？
#	{76}	top of 長 (ECE3 󰒋)	？
#	{77}	top of 㒫 (ECE4 󰐍)	？
#	{78}	亜 without the top stroke (ECE5 =口󸸤)	⿱⿻口⿰丨丨一
#	{79}	亞 without the top stroke (F5E1 󶏑)	？
#	{80}	top of 罙 [G source glyph] (ECE6 =㓁)	⿱冖八
#	{81}	inner part of 𮭷 (ECE7 =厂󰐈)	？
#	{82}	top of 書 (F2E3 󰒆)	⿱𦘒一
#	{83}	top of 帝 (ECE9 󰒑)	⿳亠丷冖
#	{84}	厂 with hooked stroke (ECEA 󰐡)	⿰丿乛
#	{85}	广 with hooked stroke (ECEB =丶󰐡)	⿰丿⿱丶乛
#	{86}	first three strokes of 灬 (ECEC 󰏴)	⿲丶丶丶
#	{87}	top of 亭 (ECED 󰊟)	⿳亠口冖
#	{88}	top of 合 (ECEE =亼)	⿵𠆢一
#	{89}	outside of 𪚧 (F5F7 󰐕)	？
#	{90}	core of 龜 [G source glyph] (F5F8 =󰋤)	？
#	{91}	core of 黽 (F5F9 󰋤)	？
#	{92}	core of 𪚧 (F5FA 󰋋)	？
#	{93}	right of 縄 (ECEF 󰑁)	⿻昌乚
#	{94}	top of 其 (ECF0 󰑱)	？
#	{95}	bottom of 𢀞 (ECF1 󰑊)	⿻尸一
#	{96}	outside of 𭾐 (ECF2 󰉽)	？
#	{97}	bottom right of 𮊫 or right of 戠 without horizontal stroke (ECF3 󰒢)	？
#	{98}	top of 曹 (F5DE 󰓛)	⿻一曲
#	{99}	top of 𢘼 (ECF5 󰐅)	⿻𦉫𠄠
#	{100}	之 without top stroke (F3BE 󰍅)	？
#	{101}	left of 癶 (ECF6 󰐘)	？
#	{102}	right of 癶 (ECF7 󰐚)	？
#	{103}	top right of 祭 (ECF8 󰒚)	？
#	{104}	top right of 𥋮 (ECF9 󰉥)	？
#	{105}	top right of 𨄛 (ECFA 󰉣)	？
#	{106}	top of 𡛹 (ECFB 󰉤)	？
#	{107}	middle of 𡙌 (ECFC =󰕯󰒚)	？
#	{108}	top of 冎 [G source glyph] (ECFD =𭁟)	？
#	{109}	left of 刋 (ECFE =千)	⿻⿱㇒一丿
#	{110}	outside of 𭁟 (F29E 󰒂)	冂
#	{111}	inside of 𭁟 (EF31 󰎀)	？
#	{112}	outside of 𦥓 (EF32 𦥓)	？
#	{113}	bottom of 𠕉 (EF33 󱦰)	⿵冂八
#	{114}	top of 坴 [G source glyph] (EF34 󱂆)	⿱土八
#	{115}	top of 𫭣 (EF35 󰐑)	⿻𰀪⺀
#	{116}	inside top of 鹿慶 (EF36 󰐈)	？
#	{117}	inside of 𠣏 [G source glyph] (EF37 =止)	⿺𠃊⺊
#	{118}	outside of 𠁅 (ECFF 󰐐)	？
#	{119}	right of 𥛈 (F2E5 󰐜)	⿻己⿱工工
#	{120}	right of 𤛒 without 口 (F2CC 󰊍)	⿻弓⿱{50}匚
#	{121}	top of 𣬋 (F5DF 󰉔)	⿱⺈罒
#	{122}	bottom of 宐 (F5E3 󴂾)	？

(F2F6 󰒺)
(F317 󶇾)
(F316 󰐧)
(F2FB 󰕆)	^⿰三丨$
(F2FC 󰕅)	^⿰丨三$
"""

class 表(_表):
	_file = "部件檢索.htm"
	簡稱 = "部件檢索"
	說明 = "來源：https://fgwang.blogspot.com/2023/10/unicode-151.html"
	patches = {"□": "!冂一", "〇": "@"}

	def normList(自, l, vt, d):
		nl = []
		for t in l:
			for k in t:
				t = t.replace(k, 自.pua.get(k, k))
			for k in t:
				t = t.replace(k, vt.get(k, k))
			for k in t:
				if ord(k) >= 0xF0000 and k in d:
					t = t.replace(k, re.split("[!@]", d[k][0])[1])
			for k in t:
				t = t.replace(k, 自.pua.get(k, k))
			for k in t:
				t = t.replace(k, vt.get(k, k))
			for k in t:
				if ord(k) >= 0xF0000 and k in d:
					t = t.replace(k, re.split("[!@]", d[k][0])[1])
			nl.append(t)
		return nl

	def 更新(自):
		d = defaultdict(list)
		f = open(自.spath,encoding="U8")
		cont = f.read()
		f.close()
		vt = eval(re.findall(r"var vt=(\{.*?\})", cont, re.MULTILINE|re.DOTALL)[0])
		自.pua = {}
		for 行 in puas.split("\n"):
			g = re.findall(r" (..)\)", 行)
			if g:
				a, b = g[0]
				自.pua[b] = a
		vt = {k: v for k, v in vt.items() if ord(k) >= 0xF0000}
		for 字 in 自.kCompatibilityVariants:
			if isCompatible(字):
				vt[字] = 自.kCompatibilityVariants[字]
		vt["󺤈"] = "⺈田儿"
		vt["󰉻"] = "甶儿"
		vt["艹"] = "卝"
		dt = eval(re.findall(r"var dt=(\[.*?\])", cont, re.MULTILINE|re.DOTALL)[0])
		for 行 in dt:
			字 = 行[0]
			js = 行[1:]
			d[字].append(js)
		for 字 in d.keys():
			d[字] = 自.normList(d[字], vt, d)
		for 字 in d.keys():
			d[字] = 自.normList(d[字], vt, d)
		自.寫(d)

