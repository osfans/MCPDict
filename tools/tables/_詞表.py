#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables._表 import HZ_STR, PATH, TARGET
import os
import re

class 表(_表):
	sets = set()
	詞表 = list()

	def 讀(自, 更新=False):
		自.sets.clear()
		自.詞表 = [f"#詞語\t音標\t解釋\n"]
		super().讀(更新)

	def 寫(自, d):
		super().寫(d)
		tpath = os.path.join(PATH, TARGET, "詞表", f"{自.簡稱}.tsv")
		if not os.path.exists(os.path.dirname(tpath)):
			os.makedirs(os.path.dirname(tpath))
		t = open(tpath, "w", encoding="U8", newline="\n")
		t.writelines(自.詞表)
		t.close()

	def 析(自, 列):
		名 = 自.簡稱
		行 = "".join(列).strip()
		if 名 == "羅田勝利":
			行 = 行.replace("＝", "?").replace(" ", "")
		elif 名 == "天柱竹林":
			行 = re.sub(r"\((.*?)\)(.*)$", r"\2\1", 行)
		groups = re.findall(rf"^([{HZ_STR}/]+)([^{HZ_STR}\(\)]+)(.*?)$", 行)
		if not groups: return
		l = list()
		cy0, pys0, js= groups[0]
		cy1 = cy0.split("/")
		pys1 = pys0.split("/")
		if len(cy1) > len(pys1):
			pys1 = pys1 + [pys1[0]] * (len(cy1) - len(pys1))
		for cy, py in zip(cy1, pys1):
			cy = cy.strip()
			pys2 = py.strip().translate(自.kTones)
			pys = re.findall(r"([^\d]+[\d ]*)", pys2)
			轉調類 = 自.info.get("字表使用調值", False)
			if js:
				if len(cy) == 1:
					zs = js
				elif "□" in cy:
					zs = f"【{cy}】[{pys2}]{js}"
				else:
					zs = f"【{cy}】{js}"
			elif len(cy) > 1:
				zs = cy
			else:
				zs = ''
			cy = cy.replace(",", "")
			pyn = len(pys)
			if pyn == 0: return
			cys = re.findall('.[0-9=+\\?*-]?', cy)
			cyn = len(cys)
			pys = [py.strip() for py in pys]
			if 轉調類: pys = [自.轉調類(py) for py in pys]
			if 0 != (cyn - pyn) != cys.count("儿"):
				自.誤.append(f"{行} 詞與音節不匹配")
				return
			自.詞表.append(f"{cy}\t{' '.join(pys)}\t{js}\n")
			if cyn - pyn != 0:
				while "儿" in cys:
					er_index = cys.index("儿")
					del cys[er_index-1:er_index+1]
					del pys[er_index-1:er_index]
			for i,z in enumerate(cys):
				yb = pys[i]
				if z == "□":
					if z[0] + yb + zs in 自.sets:
						continue
					自.sets.add(z[0] + yb + zs)
				else:
					if z[0] + yb in 自.sets:
						continue
					自.sets.add(z[0] + yb)
				mark = ""
				if len(z) > 1 and z[1] in "+-=":
					mark = z[1]
				l.append((z[0], yb + mark, zs))
				if len(z) > 1 and z[1] == "?":
					l.append(("□", yb, zs.replace(z, "□")))
		return l
