#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):

	def parse(self, fs):
		name = str(self)
		hz = ""
		yb = ""
		ipa = ""
		js = ""
		if name in ("汝城", "瑞安東山", "新界客家話", "長壽", "宜章巖泉","郴州","樂昌皈塘","嘉禾普滿","尤溪","晉江", "龍門路溪", "詔安", "道縣官话", "重慶", "樂昌三溪"):
			hz, yb, js = fs[:3]
		elif name in ("通州金沙",):
			yb, hz, js = fs[:3]
		elif name in ("江陰", "江陰新橋", "江陰申港"):
			_, hz, js, yb = fs[:4]
		elif name in ("蘇州",):
			_, hz, sm, ym, sd, js = fs[:6]
			yb = sm + ym + sd
		elif name in ("東方八所",):
			_, hz, sy, sd, js = fs[:5]
			yb = sy + sd
		elif name in ("龍游",):
			_, hz, sm, ym, dz, _, js = fs[:7]
			ipa = sm + ym + dz
		elif name in ("1900梅惠",):
			hz,_,_,yb = fs[:4]
		elif name in ("劍川金華",):
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
		elif name in ("華安髙安","五華"):
			ipa, hz, js = fs[:3]
		elif name in ("惠來隆江",):
			hz, ipa, js = fs[:3]
		elif name in ("壽寧平溪"):
			hz, yb, js = fs[:3]
			hz = hz.replace("", "□")
		elif name in ("新會會城",):
			hz, _, _, ipa = fs[:4]
		elif name in ("廈門","漳州","饒平", "遵義", "犍爲玉津", "綦江古南", "桐梓婁山關"):
			hz, _, ipa, js = fs[:4]
		elif name in ("遂昌","五華橫陂","蔡家話"):
			hz, sy, sd, js = fs[:4]
			ipa = sy + sd
		elif name in ("開化",):
			hz, js, sm, ym, sd = fs[:5]
			yb = sm + ym + sd.strip("[]")
			if re.match(r"（.*?）", js): js = js[1:-1]
		elif name in ("富陽東梓關","新登城陽"):
			_, hz, js, ipa = fs[:4]
		elif name in ("新登下港",):
			hz, ipa, js = fs[0], fs[6], fs[8]
		elif name in ("嘉善", "上海"):
			hz, sm, ym, sd, js = fs[:5]
			yb = sm + ym + sd
			if hz.endswith("-"):
				hz = hz[:-1]
				yb = yb + "-"
		elif name in ("松陽", "臨海", "泰順羅陽", "雲和", "仙居"):
			hz, _, sy, sd, js = fs[:5]
			ipa = sy + sd
		elif name in ("珠海唐家",):
			hz, sm, ym, sd, js = fs[7], fs[12], fs[13], fs[14], fs[18]
			ipa = sm + ym + sd
		elif name in ("江門",):
			hz, sm, ym, sd, jso, js = fs[7], fs[25], fs[26], fs[27], fs[11], fs[12]
			ipa = sm + ym + sd
			if jso: js = jso + "。" + js
			js = js.strip("。")
		elif name in ("江門墟頂","江門白沙","江門水南","江門沙仔尾","江門紫萊"):
			hz, sm, ym, sd, js = fs[0], fs[6], fs[7], fs[4], fs[9]
			yb = sm + ym + sd
		elif name in ("江門禮樂","江門潮連"):
			hz, sm, ym, sd, js = fs[:5]
			ipa = sm + ym + sd
		elif name in ("瑞安湖嶺",):
			_, hz, ipa, _, js = fs[:5]
		elif name in ("湖州",):
			hz, _, ipa, _, js = fs[:5]
		elif name in ("武義",):
			_, hz, _, ipa, js = fs[:5]
		elif name in ("鳳凰-新豐",):
			hz, py, _, ipa, js = fs[:5]
		elif name in ("潮州","汕頭"):
			hz, _, _, ipa, js = fs[:5]
		elif name in ("汕頭市郊"):
			hz, _, _, ipa, js = fs[:5]
		elif name in ("雷州",):
			hz, _, _, _, _, ipa = fs[:6]
			ipa = ipa.replace("˨˨˩", "˨˩")
		elif name in ("長泰",):
			_, sm, ym, sd, hz, js = fs[:6]
			ipa = sm + ym + sd
		elif name in ("普寧",):
			hz,_,js,sm,ym,sd = fs[:6]
			ipa = sm + ym + sd
		elif name in ("中山三鄕",):
			hz,sm,ym,sd, _, js = fs[:6]
			ipa = sm + ym + sd
		elif name in ("深圳南頭",):
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
		elif name in ("鶴山雅瑤",):
			hz, sm, ym, sd, _, _, _, js = fs[:8]
			ipa = sm + ym + sd
		elif name in ("揭陽",):
			hz, _, _, _, _, ipa, yd, js = fs[:8]
			yb = self.dz2dl(ipa)
			ipa = ""
			yd = yd.strip("(读)")
			if yd == "文": yb+="="
			elif yd == "白": yb+="-"
		elif name in ("台山斗山墟",):
			hz, ipa, js = fs[0], fs[12], fs[13]
		elif name in ("新會天湖",):
			hz, sm, ym, sd, js = fs[0], fs[11], fs[12], fs[13], fs[14]
			ipa = sm + ym + sd
		elif name in ("鶴山沙坪",):
			hz, sm, jy, ym, sd, js = fs[0], fs[8], fs[9], fs[10], fs[11], fs[14]
			l = list()
			for i in ym.split("，"):
				ipa = sm + jy + i + sd
				yb = self.dz2dl(ipa)
				l.append((hz, yb, js))
			return l
		elif name in ("縉雲",):
			hz, _, _, _, _, js, _, ipa = fs[:8]
		elif name in ("深圳西鄕","深圳沙井"):
			hz, _, _, _, _, _, _, ipa, js = fs[:9]
		elif name in ("新晃凳寨",):
			hz,ipa,js = fs[0], fs[9], fs[10]
		elif name in ("如東豐利",):
			hz,_,sy,_,_,_,sd,_,_,js = fs[:10]
			yb = sy + sd
		elif name in ("如東大豫",):
			hz,_,_,_,sd,js,sm,ym,_ = fs[:9]
			yb = sm + ym + sd
		elif name in ("詔安白葉","詔安霞葛"):
			hz, yb, js = fs[:3]
			if " " in yb:
				l = list()
				for y,j in zip(yb.split(" "), js.split(" ")):
					l.append((hz, y, j))
				return l
		elif name in ("陽春河口",):
			hz, sm, ym, sd, js = fs[9], fs[6], fs[7], fs[4].split("\\")[0], fs[10]
			yb = sm + ym + sd
		elif name in ("中山石岐",):
			hz, sm, ym, sd, js = fs[7], fs[12], fs[13], fs[14], fs[18]
			ipa = sm + ym + sd
		elif name in ("上饒沙溪",):
			hz, yb, _, js = fs[:4]
		elif name in ("1818漳州",):
			hz, yb = fs[0], fs[4]
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
			yb = self.normYb(yb)
			if hz in "?？☐�": hz = "□"
			return hz, yb, js
		return
