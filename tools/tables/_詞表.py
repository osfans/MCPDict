#!/usr/bin/env python3

from tables._表 import 表 as _表
from tables._表 import HZ_STR
import re

class 表(_表):
	sets = set()

	def 讀(自, 更新=False):
		自.sets.clear()
		super().讀(更新)

	def 析(自, 列):
		行 = "".join(列)
		groups = re.findall(rf"^([{HZ_STR}]+)([^{HZ_STR}\(\)]+)(.*?)$", 行)
		if not groups: return
		cy, pys, js= groups[0]
		cy = cy.strip()
		pys2 = pys.strip()
		pys = re.findall(r"([^\d]+[\d ]+?)", pys2)
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
		l = list()
		cy = cy.replace(",", "")
		for i,z in enumerate(re.findall('.[0-9=+\\?*-]?', cy)):
			if len(pys) <= i:
				自.誤.append(f"{行} 詞與音節不匹配")
				return
			yb = pys[i]
			if z != "□" and z[0] + yb in 自.sets:
				continue
			自.sets.add(z[0] + yb)
			mark = ""
			if len(z) > 1 and z[1] in "+-=":
				mark = z[1]
			l.append((z[0], yb + mark, zs))
		return l
