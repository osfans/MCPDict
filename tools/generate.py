#!/usr/bin/env python3

import sqlite3, re, os
from collections import defaultdict
from tables import *

from time import time
start = time()

dicts = defaultdict(dict)
langs = getLangs()
for lang in langs:
	lang.load(dicts)

keys = [lang.key for lang in langs]
dialects = [k for k in keys if "_" in k]
fields = ",".join(keys)
qmarks = ','.join('?' * len(keys))
INSERT = 'INSERT INTO mcpdict VALUES (%s)'% qmarks
infos = [lang.head for lang in langs]
rows = list(zip(*infos))[1:]
rows[5] = list(rows[5])
rows[5][0] = "語言數：%d<br>字數：%d<br><br>%s"%(len(dialects),len(dicts),rows[5][0])

#db
NAME='../app/src/main/assets/databases/mcpdict.db'
os.remove(NAME)
conn = sqlite3.connect(NAME)
c = conn.cursor()
c.execute("CREATE VIRTUAL TABLE mcpdict USING fts3 (%s)" % fields)
c.executemany(INSERT, rows)
for i in sorted(dicts.keys(), key=cjkorder):
	v = list(map(dicts[i].get, keys))
	c.execute(INSERT, v)
conn.commit()
conn.close()

passed = time() - start
print(f"({len(dicts):5d}) {passed:6.3f} 保存")
