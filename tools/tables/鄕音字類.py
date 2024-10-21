#!/usr/bin/env python3

from tables._表 import 表 as _表
import os
from openpyxl import load_workbook

class 表(_表):
	note = "符號說明：IPA後的“*”表示讀音可能存疑，如聲母z、dz，dʐ、ʐ歸屬存疑，陽上陽去歸屬存疑等。<br><br>聲調：[1]陰平44 [2]陽平223 [3]陰上51 [4]陽上31 [5]陰去523 [6]陽去312 [7]陰入4 [8]陽入23<br>關於調值：Edkins於19世紀五六十年代（早於《蘇州同音字彙》的成書時間）記載的蘇州話單字調分別爲：[1]陰平53 [2]陽平13 [3]陰上55 [4]陽上31 [5]陰去35 [6]陽去113 [7]陰入5 [8]陽入1，僅供參考。<br><br>來源：樛木整理自《蘇州同音常用字彙》（載於《一百年前的蘇州話》）、《蘇州同音字彙1892》、《鄉音字類》<br>	<br>說明1：《蘇州同音字彙1892》聲母w、y不區分清濁，半濁聲母m、n、l缺乏直接資料標明聲調陰陽者本表皆歸入陽調，故本表給出的陰陽調可能與實際有出入。並且韻母uah不區分[uɑʔ][uaʔ]，故該書中除有資料給出明確發音、區分者，其餘全記作[uɑʔ]。<br>此外，《鄉音字彙》聲調不分陰陽，則聲母爲次濁者除有資料支持者外，本人一般作歸入陽調處理。<br><br>說明2：	本表有部分漢字選自陸基《蘇州同音常用字彙》，書中只給出了讀音但是沒有標聲調，故對於沒有明確資料記載漢字的讀音採用今音。對於與各資料記載不一致的讀音，給出的是推測的讀音，並在注釋中標出今音或資料中的標音。<br><br>說明3：	本表多數漢字選自《鄉音字類》，該書主要記載了十九世紀蘇州話文讀音，成書於1877年，作者陸懋修。<br><br>說明4：	本表採用的擬音主要出自蔡佞《19世紀末的蘇州話》，但沒有採用ɒ、iɒ、uɒ，ɒʔ、iɒʔ、uɒʔ，依舊採用ɑ、iɑ、uɑ，ɑʔ、iɑʔ、uɑʔ<br>	<br>說明5：《蘇州同音字彙1892》中in韻分[iən][iɪn]，但分法存疑，一般以讀[iɪn]爲主，只有“人忍㣼認”明確記載讀[ȵiən]。其餘讀[-iən]者以論文《19世紀末的蘇州話》中的記載及韻書推導爲依據；<br>該時期的蘇州話語音特點：<br>① 仍有字分[o][uo]，但也在合併階段；<br>② 今e韻分[-e]和[-æ̃]，ie韻分[-ie]和[-iæ̃]，大部分[əu]讀作[u]，還有少數讀[uo]。<br>③ [yn]都讀[yən]， [øʏ]都讀[əʏ]；<br>④ [iø]有人擬作[yø]，[io]有人擬作[yo]，[ioŋ]有人擬作[yoŋ]，[ioʔ]有人擬作[yoʔ]，[iɑ̃]有人擬作[yɑ̃]（實際可能是[yɒ̃]），這是是因爲介音i受主元音影響而變圓唇。<br><br>說明6：發音盡量以其資料中記載爲准。記音以蔡佞《19世紀末的蘇州話》中的擬音爲基準。但擬聲字的讀音以資料記載爲準，不類推。<br><br>說明7：	部分元音從19世紀末至今的演變路徑（可能）：<br>ʯ→ʮ <br>ɔ→ɒ→o/ɑ→o/a（新派） <br>部分u→ᵊu→əu 部分u→β̩/v̩ 部分u→o <br>大部分uo→o 少部分uo→o→u→ᵊu→əu <br>i→iⱼ→ɨ̻ y→yⱼ <br>e→ᴇ ie→iᴇ→iɪ→i ue→uᴇ <br>ɐɵ→ɐə→ɐ→æ <br>iɐɵ→iɐə→iɐ→iæ <br>əʏ→øʏ→eɪ（新派） iʏ→y <br>æ̃→ɛ→ᴇ iɛ̃→iɛ→iᴇ→iɪ→i uæ̃→uɛ→uᴇ <br>œ̃→ø iœ̃→iø uœ̃→uø <br>iən→iɪn→in iɪn→in yən→yn <br>aŋ→ã iaŋ→iã uaŋ→uã <br>ɔŋ→ɒ̃→ɑ̃→ã（新派） iɔŋ→iɒ̃→iɑ̃→iã（新派） uɔŋ→uɒ̃→uɑ̃→uã（新派） iɔŋ→iɒ̃→iɑ̃→iã（新派） <br>部分iɪʔ→iəʔ yɪʔ→yəʔ uoʔ→oʔ<br>	<br>說明8：在蔡佞的論文《19世紀末的蘇州話》中將翹舌音擬作[tʃ]、[tʃʰ]、[ʃ]、[ʒ]、[dʒ]，爲本表所採用的翹舌音擬音。陸基的翹舌音記爲[tʂ]、[tʂʰ]、[ʂ]、[ʐ]，與傳統意義上的老派蘇州話資料記音相同，不分[dʐ][ʐ]。李軍的論文《蘇州方言字書<鄉音字類>簡介及同音字彙》中把翹舌音擬作[tʂ]、[tʂʰ]、[ʂ]、[ʐ]、[dʐ]。其實在《鄉音字類》時代，[dz][z]、[dʐ][ʐ]就已出現相混的現象了，且之後分別併入[z]、[ʐ]；1877年陸懋修編纂的《鄉音字類》不區分[dz][z]，但1892年傳教士編纂的《蘇州方言字音表》仍區分[dz][z]，[dʐ][ʐ]則兩者都區分，不過該書中指出在當時的口語中[dʐ]位於詞首時讀作[ʐ]，[dz]位於詞首時讀作[z]。"

	def update(self):
		super().update()
		self.note = self.get_note()

	def get_note(self):
		sname = self.get_fullname(self._file)
		if not os.path.exists(sname) or not sname.endswith(".xlsx"): return
		wb = load_workbook(sname, data_only=True)
		sheet = wb.worksheets[1]
		lines = list()
		for row in sheet.rows:
			fs = [j.value if j.value else "" for j in row[:50]]
			if any(fs):
				line = "\t".join(fs)
				if line:
					lines.append(line.lstrip("#"))
		return "\n".join(lines)

	def parse(self, fs):
		if len(fs) < 4: return
		hz, jt, py, js = fs[:4]
		py = py.replace("øʏ","𐞢ʏ")
		if py.endswith("="):
			js = "(書)%s" % js
		elif py.endswith("-"):
			py = py[:-1] + "="
		js = js.strip().replace("|", "｜")
		if not hz: hz = jt
		return hz, py, js

