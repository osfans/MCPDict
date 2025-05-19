#!/usr/bin/env python3

import sqlite3, os, sys
from collections import defaultdict
from time import time
from tables import *
import argparse

parser = argparse.ArgumentParser(description='Create mcpdict database')
parser.add_argument('-c', action='store_true', help='check 同音字頻', required=False)
parser.add_argument('-省', help='province to include', required=False)
args, argv = parser.parse_known_args()
start = time()

dicts = defaultdict(dict)
langs = getLangs(dicts, argv, args)
keys = [f"{lang.簡稱}" for lang in langs]
fields = [f"`{i}`" for i in keys]
CREATE = 'CREATE VIRTUAL TABLE mcpdict USING fts3 (%s)' % (",".join(fields))
INSERT = 'INSERT INTO mcpdict VALUES (%s)'% (','.join('?' * len(keys)))

#db
NAME = os.path.join(WORKSPACE, '..', 'app/src/main/assets/databases/mcpdict.db')
DIR = os.path.dirname(NAME)
if os.path.exists(NAME): os.remove(NAME)
if not os.path.exists(DIR): os.mkdir(DIR)
conn = sqlite3.connect(NAME)
c = conn.cursor()
for i in keys:
	if keys.count(i) > 1:
		print(f"{i}重名")
		sys.exit(1)
c.execute(CREATE)
for i in sorted(dicts.keys(), key=cjkorder):
	v = list(map(dicts[i].get, keys))
	c.execute(INSERT, v)

#info
keys = list(langs[辭典數 if len(keys) > 辭典數 else 1].info.keys())
keys.remove("字表格式")
keys.remove("跳過行數")
keys.remove("字表使用調值")
keys.remove("字聲韻調註列名")
CREATE = 'CREATE VIRTUAL TABLE info USING fts3 (%s)' % (",".join(keys))
INSERT = 'INSERT INTO info VALUES (%s)'% (','.join('?' * len(keys)))
c.execute(CREATE)
for lang in langs:
	v = list(map(lang.info.get, keys))
	c.execute(INSERT, v)

conn.commit()
conn.close()

passed = time() - start
print(f"({len(dicts):5d}) {passed:6.3f} 保存")
