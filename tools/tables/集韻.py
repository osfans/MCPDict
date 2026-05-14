#!/usr/bin/env python3

from tables._表 import 表 as _表
import re

class 表(_表):
	全稱 = "集韻"
	說明 = "來源：<a href=https://github.com/guavajuice/qieyun/blob/main/public/data/zhup_hyun.csv/>切韵查詢</a>"
	字書 = True
	文件名 = "集韻.md"
	卷, 韻, 切 = "", "", ""

	def 統(自, 行):
		行 = _表.統(自, 行)
		if 行.startswith("### "): 自.反切 = 行.strip("# ")
		elif 行.startswith("## "): 自.韻 = 行.strip("# ")
		elif 行.startswith("# "): 自.卷 = 行.strip("# ")
		return 行
	
	def 析(自, 列):
		if not 列[0].endswith("`"): return
		字, 註 = 列[0].split("`")[:2]
		音 = f"{自.卷[6]}{自.卷[4]}{自.韻[0]}。{自.反切.split(" ")[0].rstrip("切")}"
		字 = 字.replace("=", "").replace("[", "").replace("]", "")
		l = list()
		for i, ids in re.findall(r"(.)(\(.*?\))?", 字):
			l.append((i, 音, ids + 註))
		return l
