#!/usr/bin/env python3

import sqlite3, os, sys
from collections import defaultdict
from time import time
from tables import *
import argparse

parser = argparse.ArgumentParser(description='Create mcpdict database')
parser.add_argument('-c', action='store_true', help='檢查同音字', required=False)
parser.add_argument('-s', action='store_true', help='計算相似度', required=False)
parser.add_argument('-省', help='province to include', required=False)
parser.add_argument('-o', '--output', help='output tsv', required=False)
parser.add_argument('-i', '--info', action='store_true', help='output info html', required=False, default=False)
args, argv = parser.parse_known_args()
start = time()

字數 = 0

def dumpInfo(langs):
	lines = list()
	lines.append(open("info_header.html", "r", encoding="U8").read())
	headers = ["序號","語言", "簡稱", "地點", "經緯度", "地圖集二分區", "音典分區", "陳邡分區", "版本","字表來源", "參考文獻", "補充閲讀", "字數", "□數", "音節數", "不帶調音節數"]
	for header in headers:
		lines.append(f"\t\t\t\t\t<th onclick='sortTableByColumn({headers.index(header)})'>{header}</th>\n")
	lines.append("\t\t\t\t</tr>\n\t\t\t</thead>\n\t\t\t<tbody>\n")
	count = 0
	for lang in langs[1:]:
		if not lang.info.get("地圖集二分區", ""): continue
		lines.append("\t\t\t\t<tr>\n")
		for i in headers:
			v = lang.info.get(i, "")
			if i == "序號":
				count += 1
				v = str(count)
			if v is None: v = ""
			lines.append(f"\t\t\t\t\t<td>{v}</td>\n")
		lines.append("\t\t\t\t</tr>\n")
	lines.append("\t\t\t</tbody>\n\t\t</table>\n\t</body>\n</html>")
	curdir = os.path.dirname(__file__)
	curpath = os.path.join(curdir, "info.html")
	open(curpath, "w",encoding="U8",newline="\n").writelines(lines)

#db
if args.output:
	langs, 高頻字 = getLangs(list(), argv, args)
	字數 = langs[-1].info["字數"]
else:
	NAME = os.path.join(WORKSPACE, '..', 'app/src/main/assets/databases/mcpdict.db')
	DIR = os.path.dirname(NAME)
	if os.path.exists(NAME): os.remove(NAME)
	if not os.path.exists(DIR): os.mkdir(DIR)
	conn = sqlite3.connect(NAME)
	c = conn.cursor()
	items = list()
	langs, 高頻字 = getLangs(items, argv, args)
	keys = [f"{lang.簡稱}" for lang in langs]
	for i in keys:
		if keys.count(i) > 1:
			print(f"{i}重名")
			sys.exit(1)
	fields = ["字組", "語言", "讀音", "註釋"]
	tokens = "□－〈〉［］（）"
	CREATE = 'CREATE VIRTUAL TABLE langs USING fts5 (%s, columnsize=0, tokenize="unicode61 remove_diacritics 0 tokenchars \'%s\' ")' % (",".join(fields), tokens)
	INSERT = 'INSERT INTO langs VALUES (%s)'% (','.join('?' * len(fields)))
	c.execute(CREATE)
	c.executemany(INSERT, items)
	del items
	字書 = None
	if len(argv) == 1:
		字數 = langs[-1].info["字數"]
	else:
		dicts = defaultdict(dict)
		fields, 字書 = getDicts(dicts)
		CREATE = 'CREATE VIRTUAL TABLE mcpdict USING fts5 (%s, columnsize=0, tokenize="unicode61 tokenchars \'%s\'")' % (",".join(fields), tokens)
		INSERT = 'INSERT INTO mcpdict VALUES (%s)'% (','.join('?' * len(fields)))
		c.execute(CREATE)
		c.executemany(INSERT, (tuple(map(dicts[i].get, fields)) for i in sorted(dicts.keys(), key=lambda x:((高頻字.index(x) if x in 高頻字 else 0xffff),-len(dicts[x]),cjkorder(x)))))
		字數 = len(dicts)
		del dicts
	langs[0].info["字數"] = 字數
	if 字書:
		langs.extend(字書)
	keys = list(langs[1].info.keys())
	keys.remove("字表格式")
	keys.remove("跳過行數")
	keys.remove("字表使用調值")
	keys.remove("字聲韻調註列名")
	CREATE = 'CREATE VIRTUAL TABLE info USING fts5 (%s, columnsize=0)' % (",".join(keys))
	INSERT = 'INSERT INTO info VALUES (%s)'% (','.join('?' * len(keys)))
	c.execute(CREATE)
	c.executemany(INSERT, (tuple(map(lang.info.get, keys)) for lang in langs))

	conn.commit()
	conn.close()

if args.info:
	dumpInfo(langs)

passed = time() - start
print(f"({字數:5d}) {passed:6.3f} 保存")
