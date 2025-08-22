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
args, argv = parser.parse_known_args()
start = time()

字數 = 0

#db
if not args.output:
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
	fields = ["字組", "讀音", "註釋", "語言"]
	CREATE = 'CREATE VIRTUAL TABLE langs USING fts3 (%s)' % (",".join(fields))
	INSERT = 'INSERT INTO langs VALUES (%s)'% (','.join('?' * len(fields)))
	c.execute(CREATE)
	c.executemany(INSERT, items)
	del items
	字書 = None
	if len(argv) != 1:
		dicts = defaultdict(dict)
		fields, 字書 = getDicts(dicts)
		CREATE = 'CREATE VIRTUAL TABLE mcpdict USING fts3 (%s)' % (",".join(fields))
		INSERT = 'INSERT INTO mcpdict VALUES (%s)'% (','.join('?' * len(fields)))
		c.execute(CREATE)
		c.executemany(INSERT, (list(map(dicts[i].get, fields)) for i in sorted(dicts.keys(), key=lambda x:((高頻字.index(x) if x in 高頻字 else 0xffff),-len(dicts[x]),cjkorder(x)))))
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
	CREATE = 'CREATE VIRTUAL TABLE info USING fts3 (%s)' % (",".join(keys))
	INSERT = 'INSERT INTO info VALUES (%s)'% (','.join('?' * len(keys)))
	c.execute(CREATE)
	c.executemany(INSERT, (list(map(lang.info.get, keys)) for lang in langs))

	conn.commit()
	conn.close()

passed = time() - start
print(f"({字數:5d}) {passed:6.3f} 保存")
