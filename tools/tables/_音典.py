#!/usr/bin/env python3

from tables._表 import 表 as _表

class 表(_表):

	def parse(self, fs):
		name = str(self)
		hz = ""
		yb = ""
		ipa = ""
		js = ""
		if name in ("汝城", "瑞安東山", "香港新界", "長壽", "宜章巖泉","郴州","樂昌皈塘","嘉禾普滿","尤溪"):
			hz, yb, js = fs[:3]
		elif name in ("南通金沙",):
			hz, js, yb = fs[:3]
		elif name in ("江陰", "江陰新橋", "江陰申港"):
			_, hz, js, yb = fs[:4]
		elif name in ("1900梅惠",):
			hz,_,_,yb = fs[:4]
		elif name in ("劍川金華白語",):
			hz, sy, sd, js = fs[:4]
			yb = sy + sd
		elif name in ("泉州",):
			hz, py, yb, js = fs[:4]
			py, sd = self.splitSySd(py)
			yb, _ = self.splitSySd(yb)
			yb += sd
		elif name in ("1926綜合",):
			hz,_,_,yb,js = fs[:5]
		elif name in ("蒼南錢庫",):
			sm,ym,sd,hz,js = fs[:5]
			if sd == "轻声": sd = "0"
			yb = sm + ym + sd
		elif name in ("1890會城",):
			hz,_,_,sm,ym,js = fs[:6]
			yb = sm + ym
		elif name in ("貴陽",):
			hz, _, _, ipa, js = fs[:6]
		elif name in ("樂淸"):
			_, sm, ym, sd, hz, js = fs[:6]
			yb = sm + ym + sd
		elif name in ("淸末溫州",):
			_,hz,sy,_,_,sd,js = fs[:7]
			yb = sy + sd
		elif name in ("眞如南",):
			_, _, hz, js, sm, ym, sd = fs[:7]
			yb = sm + ym + sd
		#ipa
		elif name in ("五峯", "恩平恩城","台山台城"):
			hz, ipa = fs[:2]
		elif name in ("華安高安","五華"):
			ipa, hz, js = fs[:3]
		elif name in ("惠來隆江",):
			hz, ipa, js = fs[:3]
		elif name in ("新會會城",):
			hz, _, _, ipa = fs[:4]
		elif name in ("廈門","漳州","饒平", "遵義", "犍爲玉津", "綦江古南", "桐梓婁山關"):
			hz, _, ipa, js = fs[:4]
		elif name in ("遂昌","五華橫陂"):
			hz, sy, sd, js = fs[:4]
			ipa = sy + sd
		elif name in ("富陽東梓關",):
			_, hz, js, ipa = fs[:4]
		elif name in ("松陽", "臨海", "泰順羅陽", "雲和", "仙居"):
			hz, _, sy, sd, js = fs[:5]
			ipa = sy + sd
		elif name in ("江門禮樂","江門潮連"):
			hz, sm, ym, sd, js = fs[:5]
			ipa = sm + ym + sd
		elif name in ("瑞安湖嶺",):
			_, hz, ipa, _, js = fs[:5]
		elif name in ("湖州",):
			hz, _, ipa, _, js = fs[:5]
		elif name in ("武義",):
			_, hz, _, ipa, js = fs[:5]
		elif name in ("鳳凰-新豐","潮州","汕頭"):
			hz, _, _, ipa, js = fs[:5]
		elif name in ("雷州",):
			hz, _, _, _, _, ipa = fs[:6]
		elif name in ("長泰",):
			_, sm, ym, sd, hz, js = fs[:6]
			ipa = sm + ym + sd
		elif name in ("普寧",):
			hz,_,js,sm,ym,sd = fs[:6]
			ipa = sm + ym + sd
		elif name in ("中山三鄕",):
			hz,sm,ym,sd, _, js = fs[:6]
			ipa = sm + ym + sd
		elif name in ("南山南頭",):
			hz, _, _, _, ipa, js = fs[:6]
		elif name in ("通東餘東",):
			hz, _, _, sy, _, sd, js = fs[:7]
			sy = sy.lstrip("ʔ")
			ipa = sy + sd
		elif name in ("南寧", "南寧亭子"):
			_, hz, _, ipa, _, js, c = fs[:7]
			js = c + js
		elif name in ("蒼南蒲門",):
			hz, sy, sd, _, _, _, js = fs[:7]
			ipa = sy + sd
		elif name in ("開平沙塘",):
			hz, _, _, _, js, sm, ym, sd = fs[:8]
			ipa = sm + ym + sd
		elif name in ("縉雲",):
			hz, _, _, _, _, js, _, ipa = fs[:8]
		elif name in ("寶安西鄕","寶安沙井"):
			hz, _, _, _, _, _, _, ipa, js = fs[:9]
		elif name in ("新晃凳寨",):
			hz,_,_,_,_,_,_,ipa,js = fs[:9]
		elif name in ("如東豐利",):
			hz,_,sy,_,_,_,sd,_,_,js = fs[:10]
			yb = sy + sd
		elif name in ("如東大豫",):
			hz,_,_,_,sd,js,sm,ym,_ = fs[:9]
			yb = sm + ym + sd
		elif len(fs) >= 4:
			hz, _, ipa, js = fs[:4]
		elif len(fs) == 2:
			hz, yb = fs[:2]
		else:
			hz, yb, js = fs[:3]
		if hz:
			if ipa:
				yb = self.dz2dl(ipa)
			if len(hz) != 1 or not yb: return
			if hz in "?？☐�": hz = "□"
			return hz, yb, js
		return
